Feature: User changes a todo instance

Background:
Given There is an instance of "todos"
And the "title" is "Charge Fees"
And the binary value of "doneStatus" is "false"
And the "description" is "Demand for client fees"
And the instance is created on the server

Scenario Outline: User changes a valid field of the todo instance
Given I want to modify the instance of "todos" with new values
And the binary value of "doneStatus" is "<doneStatus>"
When I send a request to the server to modify the instance
Then the "doneStatus" is equal to "<doneStatus>"
And the "title" is equal to "Charge Fees"
And the "description" is equal to "Demand for client fees"

Examples: Todo Fields Values
| doneStatus |
| false |
| true |

Scenario Outline: User changes multiple valid fields of the todo instance
Given I want to modify the instance of "todos" with new values
And the binary value of "doneStatus" is "<doneStatus>"
And the "description" is "<description>"
When I send a request to the server to modify the instance
Then the "doneStatus" is equal to "<doneStatus>"
And the "title" is equal to "Charge Fees"
And the "description" is equal to "<description>"

Examples: Todo Fields Values
| doneStatus | description |
| false | Demand for client fees |
| true | Pay the monthly fees |
| false | Change client's name |

Scenario Outline: User changes a field of a non-existent todo instance
Given I want to modify the instance of "todos" with new values
And the binary value of "doneStatus" is "<doneStatus>"
And the affected instance is non-existent
When I send a request to the server to modify the instance
Then I receive an error not found

Examples: Todo Fields Values
| doneStatus |
| false |
| true |
| false |