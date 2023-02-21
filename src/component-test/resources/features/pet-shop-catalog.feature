Feature: Pet Shop Catalog

  Scenario: Adding a pet
    Given the following pets are available in the store:
      | name            | description    | type | breed      | price   |
      | Captain Patches | No comment     | Cat  | Moggy      | $200    |
      | The Flash       | Very fast      | Cat  | Mutant     | $10000  |
      | Krypto          | From Krypton   | Dog  | Kryptonian | $100000 |
      | Nemo            | Friend of Dori | Fish | Goldfish   | $15     |
    When I add a pet:
      | name     | description   | type | breed         | price |
      | Garfield | Likes lasange | Cat  | Persian Tabby | $10   |
    And I search for "Garfield"
    Then the following result should be returned:
      | name     | description   | type | breed         | price |
      | Garfield | Likes lasange | Cat  | Persian Tabby | $10   |

  Scenario: Searching for a pet
    Given the following pets are available in the store:
      | name            | description    | type | breed      | price   |
      | Captain Patches | No comment     | Cat  | Moggy      | $200    |
      | The Flash       | Very fast      | Cat  | Mutant     | $10000  |
      | Krypto          | From Krypton   | Dog  | Kryptonian | $100000 |
      | Nemo            | Friend of Dori | Fish | Goldfish   | $15     |
    When I search for "Nemo"
    Then the following result should be returned:
      | name | description    | type | breed    | price |
      | Nemo | Friend of Dori | Fish | Goldfish | $15   |

  Scenario: Pagination
    Given the following pets are available in the store:
      | name            | description    | type | breed      | price   |
      | Captain Patches | No comment     | Cat  | Moggy      | $200    |
      | The Flash       | Very fast      | Cat  | Mutant     | $10000  |
      | Krypto          | From Krypton   | Dog  | Kryptonian | $100000 |
      | Nemo            | Friend of Dori | Fish | Goldfish   | $15     |
    When I ask for page "1" using a page size of "2" records
    Then the following results should be returned:
      | name            | description | type | breed  | price  |
      | Captain Patches | No comment  | Cat  | Moggy  | $200   |
      | The Flash       | Very fast   | Cat  | Mutant | $10000 |

  Scenario: Updating a pet
    Given the following pet is available in the store:
      | name | description    | type | breed    | price |
      | Nemo | Friend of Dori | Fish | Goldfish | $15   |
    When I make a request to update the price of "Nemo" to "$20"
    Then the following result should be returned:
      | name | price |
      | Nemo | $20   |

  Scenario: Deleting a pet
    Given the following pet is available in the store:
      | name | description    | type | breed    | price |
      | Nemo | Friend of Dori | Fish | Goldfish | $15   |
    When I make a request to delete "Nemo"
    And I search for "Nemo"
    Then nothing should be returned

  Scenario: Adding a duplicate pet
    Given I add a pet:
      | name    | description       | type    | breed | price |
      | Pikachu | Friend of Satoshi | Pokemon | Mouse | $1500 |
    When I add a pet:
      | name    | description       | type    | breed | price |
      | Pikachu | Friend of Satoshi | Pokemon | Mouse | $1500 |
    Then no pet should be added

  Scenario: Deleting a pet that does not exist
    When I make a request to delete "Cringer"
    Then no pet "Cringer" should be deleted

  Scenario: Updating a pet that does not exist
    When I make a request to update the price of "Cringer" to "$20"
    Then no pet "Cringer" should be updated
