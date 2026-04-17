const path = require('path');
const fs = require("fs");
const { Client } = require("pg");
const { mergeSchemas, createSchema } = require('genson-js');

/**
 * Ensure required environment variables exist
 */
const requiredEnvVars = [
  'HOST',
  'PORT',
  'DB',
  'SCHEMA',
  'DB_USERNAME',
  'DB_PASSWORD',
  'OUTPUT',
  'SSL_CA_FILE',
];

for (const key of requiredEnvVars) {
  if (!process.env[key]) {
    console.error(`Missing required environment variable: ${key}`);
    process.exit(1);
  }
}

/**
 * Ensure OUTPUT directory exists
 */
const outputDir = path.resolve(process.env.OUTPUT);

try {
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
    console.log(`Created output directory: ${outputDir}`);
  }
} catch (err) {
  console.error(`Failed to create OUTPUT directory: ${outputDir}`);
  console.error(err.message);
  process.exit(1);
}


(async () => {

  const caPem = fs.readFileSync(process.env.SSL_CA_FILE, 'utf8');
  const ssl = {
          ca: caPem,
          rejectUnauthorized: false
  };


  const connectionString =
  `postgresql://${encodeURIComponent(process.env.DB_USERNAME)}` +
  `:${encodeURIComponent(process.env.DB_PASSWORD)}` +
  `@${process.env.HOST}:${process.env.PORT}` +
  `/${process.env.DB}` +
  `?search_path=${encodeURIComponent(process.env.SCHEMA)}` +
  `&sslmode=prefer` +
  `&uselibpqcompat=true`;

  /**
   *  * PostgreSQL client
   *   */
  const clientConfig = {
    connectionString: connectionString,
    ssl: ssl
  };


  let json = await pgToJson(clientConfig);

  if (process.env['METADATA']) {
		referenceFilename = process.env['METADATA']
		if (fs.existsSync(referenceFilename)) {
      console.log("Merging metadata from " + referenceFilename);
			referenceData = JSON.parse(fs.readFileSync(referenceFilename));
			json = mergeReferenceData(json,referenceData);
		}
    else {
      console.log("Metadata set to " + process.env['METADATA'] + " but file not found");
    }
	}
  else {
    console.log("Metadata not set");
  }

  generateOutputFiles(json, outputDir);


})();

async function pgToJson(dbConfig) {
  console.log("Connecting to db");
  const client = new Client(dbConfig);
  await client.connect();

  // -----------------------------------------------------
  // Fetch tables
  // -----------------------------------------------------
  const tables = await client.query(`
      SELECT table_name
      FROM information_schema.tables
      WHERE table_schema = 'public'
      `);

  let json = {'tables': []};

  // -----------------------------------------------------
  // Process each table
  // -----------------------------------------------------
  var ignoreTables = [];
  if (process.env.IGNORETABLES) {
    ignoreTables = process.env.IGNORETABLES.split("|");
  }
  console.log(ignoreTables);
  const filteredTables = tables.rows.filter(t => ignoreTables.indexOf(t.table_name) == -1);
  console.log(filteredTables);

  for (const t of filteredTables) {
    console.log("Processing table " + t.table_name);
    const tableName = t.table_name;

    // Get columns
    const cols = await client.query(
        `
        SELECT column_name, data_type
        FROM information_schema.columns
        WHERE table_name = $1 AND table_schema = 'public'
        `,
        [tableName]
        );

    table = { 'name': tableName, 'fields': []};

    for (const col of cols.rows) {
      const colName = col.column_name;
      const colType = col.data_type;


      // ----------------------------------------------
      // JSONB → custom function
      // ----------------------------------------------
      if (colType === "jsonb") {
        field = {'name': colName, 'type': 'complex', 'description': '', 'sar': false}
        table.fields.push(field);
        const jsonSchema = await processJsonbField(client, tableName, colName);

        for(i = 0; i < jsonSchema.length; i++) {
          jsonSchema[i].description = '';
          jsonSchema[i].sar = true;
          jsonSchema[i].entity = tableName;
          jsonSchema[i].mandatory = true;
          jsonSchema[i].example = '';
          jsonSchema[i].comments = '';
          table.fields.push(jsonSchema[i]);
        }
      }
      else {
        field = {'name': colName, 'type': mapPgTypeToHumanReadable(colType), 'description': '', 'sar': true, 'entity': tableName, 'mandatory': true, 'example': '', 'comments': ''}
        table.fields.push(field);
      }

    }

    json.tables.push(table);

  }

  await client.end();
  return json;
}

function mapPgTypeToHumanReadable(pgType) {
  const map = {
integer: "integer",
         bigint: "integer",
         numeric: "decimal",
         double: "decimal",
         boolean: "boolean",
         text: "string",
         varchar: "string",
         "character varying": "string",
         date: "date",
         timestamp: "dateTime",
         "timestamp without time zone": "dateTime",
  };
  return map[pgType] || "string";
}


async function processJsonbField(client, tablename, fieldname) {

  console.log("Processing jsonb field " + fieldname + " on table " + tablename);

  var sql = `select ${fieldname} from ${tablename} limit 100`;

  console.log("Querying database with " + sql);

  const res = await client.query(sql);
  var schema = {};

  for(i = 0; i < res.rows.length; i++) {
    console.log("Processing row " + i);
    thisSchema = createSchema(res.rows[i]);
    if (i > 0) {
      schema = mergeSchemas([schema, thisSchema]);
    }
    else {
      schema = thisSchema;
    }
  }

  return flattenJsonSchema(schema);
}

/**
 * Flattens a JSON Schema into an array of { name, type }
 * Supports: object, array, oneOf, anyOf, allOf
 * Excludes: any schema or union member whose type is 'null'
 */
function flattenJsonSchema(schema) {
  const fields = new Map();

  function normalizeType(type) {
    if (Array.isArray(type)) {
      return type.filter(t => t !== 'null');
    }
    return type === 'null' ? [] : [type];
  }

  function mergeField(name, types) {
    if (!types.length) return;

    const existing = fields.get(name) || new Set();
    types.forEach(t => existing.add(t));
    fields.set(name, existing);
  }

  function walk(node, path) {
    if (!node || typeof node !== 'object') return;

    // ---- Handle combinators ----
    if (node.oneOf || node.anyOf || node.allOf) {
      const variants = node.oneOf || node.anyOf || node.allOf;
      variants.forEach(sub => walk(sub, path));
      return;
    }

    // ---- Handle object ----
    if (node.type === 'object' && node.properties) {
      for (const [key, value] of Object.entries(node.properties)) {
        const nextPath = path ? `${path}.${key}` : key;
        walk(value, nextPath);
      }
      return;
    }

    // ---- Handle array ----
    if (node.type === 'array' && node.items) {
      const arrayPath = path ? `${path}[]` : '[]';
      walk(node.items, arrayPath);
      return;
    }

    // ---- Handle leaf ----
    const types = normalizeType(node.type);
    mergeField(path, types);
  }

  walk(schema, '');

  // Convert map → array
  return Array.from(fields.entries()).map(([name, typeSet]) => ({
        name,
        type: Array.from(typeSet).join('|')
        }));
}


function generateOutputFiles(json, outputDir) {
  const csvRows = [];
  const htmlRows = [];

  // Headers
  const headers = ['entity', 'element', 'description', 'type', 'example value', 'mandatory', 'sar', 'comments'];

  csvRows.push(headers.join(','));
  htmlRows.push(`<table>`);
  htmlRows.push(`<thead>`);
  htmlRows.push(`<tr> <th scope="col">${headers.join('</th><th scope="col">')} </th></tr>`);
  htmlRows.push(`</thead>`);
  htmlRows.push(`<tbody>`);

  for (const table of json.tables || []) {
    for (const field of table.fields || []) {
      const row = [
        table.name,
        field.name,
        field.description || '',
        field.type,
        field.example,
        String(field.mandatory),
        field.sar,
        field.comments
      ];

      // CSV
      csvRows.push(row.map(escapeCsv).join(','));

      // Markdown
      htmlRows.push(`<tr><td>${row.join('</td><td>')} </td></tr>`);
    }
  }
  htmlRows.push(`</tbody>`);
  htmlRows.push(`</table>`);

  // Write files
  fs.writeFileSync(
    path.join(outputDir, 'data-dictionary.csv'),
    csvRows.join('\n'),
    'utf8'
  );

  fs.writeFileSync(
    path.join(outputDir, 'data-dictionary.html'),
    htmlRows.join('\n'),
    'utf8'
  );

  fs.writeFileSync(
    path.join(outputDir, 'data-dictionary.json'),
    JSON.stringify(json),
    'utf8'
  );

  console.log('CSV, HTML fragment and JSON files generated');
}

/**
 * Escapes CSV values safely
 */
function escapeCsv(value) {
  if (typeof value !== 'string') return value;
  if (value.includes(',') || value.includes('"') || value.includes('\n')) {
    return `"${value.replace(/"/g, '""')}"`;
  }
  return value;
}



/**
 * Updates description and sar in targetSchema
 * using values from sourceSchema when matches are found
 */
function mergeReferenceData(targetSchema, sourceSchema) {
  // Build lookup map from source
  const sourceMap = new Map();

  for (const table of sourceSchema.tables || []) {
    console.log("Processing " + table.name);
    for (const field of table.fields || []) {
      const key = `${table.name}.${field.name}`;
      sourceMap.set(key, field);
      console.log("Added " + key);
    }
  }

  // Apply updates to target
  for (const table of targetSchema.tables || []) {
    for (const field of table.fields || []) {
      const key = `${table.name}.${field.name}`;
      const sourceField = sourceMap.get(key);

      console.log("Looking for " + key);

      if (!sourceField) continue;

      console.log("Found it!");

      if (sourceField.description !== undefined) {
        console.log("Found description for ${key} in metadata");
        field.description = sourceField.description;
      }

      if (sourceField.entity !== undefined) {
        field.entity = sourceField.entity;
      }

      if (sourceField.mandatory !== undefined) {
        field.mandatory = sourceField.mandatory;
      }

      if (sourceField.example !== undefined) {
        field.example = sourceField.example;
      }

      if (sourceField.comments !== undefined) {
        field.comments = sourceField.comments;
      }

      if (sourceField.sar !== undefined) {
        field.sar = sourceField.sar;
      }
    }
  }

  return targetSchema;
}

