Feature: User changes a todo instance

Background:
Given There is an instance of "todos"
And the "title" is "Charge Fees"
And the binary value of "doneStatus" is "false"
And the "description" is "Demand for client fees"
And the instance is created on the server

Scenario Outline: User changes the todo instance with a valid field
Given I want to modify the instance of "todos" with new values
And the binary value of "doneStatus" is "<doneStatus>"
When I send a request to the server to modify the instance
Then I should see the mod instance in my "todos" list successfully

Examples: Todo Fields Values
| doneStatus |
| false |
| true |

Scenario Outline: User changes the todo instance with multiple valid fields
Given I want to modify the instance of "todos" with new values
And the binary value of "doneStatus" is "true"
And the "description" is "<description>"
When I send a request to the server to modify the instance
Then I should see the additionnal instance in my "todos" list successfully

Examples: Todo Fields Values
| description |
| Demand for client fees |
| Pay the monthly fees |
| Change client's name |

Scenario Outline: User creates a todo instance with missing mandatory fields
Given I want to create an instance of "todos"
And the binary value of "doneStatus" is "<doneStatus>"
When I send a request to the server to create the instance
Then I should not see the additionnal instance in my "todos" list

Examples: Todo Fields Values
| doneStatus |
| false |
| true |
| false |