import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
class JavaPoiDocumentPlaceholderReplacementService {

//    public void generateDocFromTemplate() {
//        ClassPathResource resource = new ClassPathResource("NAT Recall Part A London - obtained 131021 edited.doc");
//
//
////      InputStream is = DocProducer.class.getResourceAsStream(templatePath);
//        HWPFDocument doc = null;
//        try {
//            doc = new HWPFDocument(resource.getInputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Range range = doc.getRange();
//
//        //Create a map of values for the template
//        Map<String, String> map = new HashMap<>();
//        map.put("forename", "Jack");
//        // "surname" to "Maloney"
//
//        //把range范围内的${}替换
//        for (Map.Entry<String, String> next : map.entrySet()) {
//            range.replaceText("$" + next.getKey(),
//                next.getValue()
//            );
//        }
//
//        try {
//            OutputStream os = new
//                FileOutputStream("/Users/jack.maloney/Development/make-recall-decision-api/src/main/resources/NAT Recall Part A London - obtained 131021 edited.doc");
//            doc.write(os);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private static final String SOURCE_FILE = "NAT Recall Part A London original1.doc";
    private static final String OUTPUT_FILE = "new-NAT Recall Part A London - obtained 131021 edited.doc";

    public void generateDocFromTemplate() throws Exception {
        JavaPoiDocumentPlaceholderReplacementService instance = new JavaPoiDocumentPlaceholderReplacementService();
        try (HWPFDocument doc = instance.openDocument(SOURCE_FILE)) {
            if (doc != null) {
                HWPFDocument newDoc = instance.replaceText(doc, "forename", "Jack");
                instance.saveDocument(newDoc, OUTPUT_FILE);
            }
        }
    }

    private HWPFDocument replaceText(HWPFDocument doc, String findText, String replaceText) {
        Range range = doc.getRange();
        for (int numSec = 0; numSec < range.numSections(); ++numSec) {
            Section sec = range.getSection(numSec);
            for (int numPara = 0; numPara < sec.numParagraphs(); numPara++) {
                Paragraph para = sec.getParagraph(numPara);
                for (int numCharRun = 0; numCharRun < para.numCharacterRuns(); numCharRun++) {
                    CharacterRun charRun = para.getCharacterRun(numCharRun);
                    String text = charRun.text();
                    if (text.contains(findText)) {
                        charRun.replaceText(findText, replaceText);
                    }
                }
            }
        }
        return doc;
    }

    private HWPFDocument openDocument(String file) throws Exception {
        ClassPathResource resource = new ClassPathResource(file);

//        URL res = getClass().getClassLoader().getResource(file);

        HWPFDocument document = null;
//        if (res != null) {
            document = new HWPFDocument(resource.getInputStream());
//            document = new HWPFDocument(new POIFSFileSystem(
//                new File(res.getPath())));
//        }
        return document;
    }

    private void saveDocument(HWPFDocument doc, String file) {
        try (FileOutputStream out = new FileOutputStream(file)) {
            doc.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
