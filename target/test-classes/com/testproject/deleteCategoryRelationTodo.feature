Feature: User add a category relation to a todo instance

Background:
Given There is an instance of "todos"
And the "title" is "Charge Fees"
And the binary value of "doneStatus" is "false"
And the "description" is "Demand for client fees"
And the instance is created on the server
And the instance is related to the "categories" instance "1"
And the instance is related to the "categories" instance "2"

Scenario Outline: User removes a todo instance from an existing category
Given I want to modify the instance of "todos" with a relation with "categories"
And the related instance is "<id>"
When I send a request to the server to delete the relation
Then I do not see the relation between the two

Examples: Category ids
| id |
| 1 |
| 2 |

Scenario Outline: User removes a todo instance from two existing categories
Given I want to modify the instance of "todos" with a relation with "categories"
And the related instance is "<id1>"
When I send a request to the server to delete the relation
Then I do not see the relation between the two
Given I want to modify the instance of "todos" with a relation with "categories"
And the related instance is "<id2>"
When I send a request to the server to delete the relation
Then I do not see the relation between the two

Examples: Category ids
| id1 | id2 |
| 1 | 2 |
| 2 | 1 |

Scenario Outline: User removes a todo instance to a non-existent category
Given I want to modify the instance of "todos" with a relation with "categories"
And the related instance is non-existent
When I send a request to the server to delete the relation
Then I receive an error not found