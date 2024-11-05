Feature: User creates a todo instance

Background:
Given There are "0" todos instances in my list

Scenario Outline: User creates a todo instance with valid fields
Given I want to create an instance of "todos"
And the "title" is "<title>"
And the binary value of "doneStatus" is "<doneStatus>"
And the "description" is "<description>"
When I send a request to the server to create the instance
Then I should see the additionnal instance in my "todos" list successfully

Examples: Todo Fields Values
| title | doneStatus | description |
| Charge Fees | false | Demand for client fees |
| Pay Fees | true | Pay the monthly fees |
| Change Name | false | Change client's name |

Scenario Outline: User creates a todo instance with valid fields and missing optional fields
Given I want to create an instance of "todos"
And the "title" is "<title>"
And the binary value of "doneStatus" is "<doneStatus>"
When I send a request to the server to create the instance
Then I should see the additionnal instance in my "todos" list successfully
And the "description" is equal to ""

Examples: Todo Fields Values
| title | doneStatus |
| Charge Fees | false |
| Pay Fees | true |
| Change Name | false |

Scenario Outline: User creates a todo instance with missing mandatory fields
Given I want to create an instance of "todos"
And the binary value of "doneStatus" is "<doneStatus>"
When I send a request to the server to create the instance
Then I receive an error of invalid request
And I do not see any additionnal instance in my "todos" list

Examples: Todo Fields Values
| doneStatus |
| false |
| true |