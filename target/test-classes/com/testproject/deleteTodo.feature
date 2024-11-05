Feature: User deletes a todo instance

Background:
Given There is an instance of "todos"
And the "title" is "Charge Fees"
And the binary value of "doneStatus" is "false"
And the "description" is "Demand for client fees"
And the instance is created on the server
And There are "1" todos instances in my list

Scenario Outline: User deletes an existing todo instance
Given I want to delete the instance of "todos"
When I send a request to the server to delete the instance
Then I should see the instance removed from my "todos" list successfully

Scenario Outline: User changes multiple valid fields of the todo instance, then deletes it
Given I want to modify the instance of "todos" with new values
And the binary value of "doneStatus" is "<doneStatus>"
And the "description" is "<description>"
When I send a request to the server to modify the instance
Then the "doneStatus" is equal to "<doneStatus>"
And the "title" is equal to "Charge Fees"
And the "description" is equal to "<description>"
Given I want to delete the instance of "todos"
When I send a request to the server to delete the instance
Then I should see the instance removed from my "todos" list successfully

Examples: Todo Fields Values
| doneStatus | description |
| false | Demand for client fees |
| true | Pay the monthly fees |
| false | Change client's name |

Scenario Outline: User deletes a non-existent todo instance
Given I want to delete the instance of "todos"
And the affected instance is non-existent
When I send a request to the server to modify the instance
Then I receive an error not found
And I do not see any change in my "todo" list