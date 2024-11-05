Feature: User creates a todo instance

Background:
Given There are "2" todos instances in my list

Scenario: User creates a todo instance with valid fields
When I send a request to the server with those fields
    | Field | Value |
    | title | <title> |
    | doneStatus | <doneStatus> |
    | description | <description> |
Then I should see the additionnal todo instance in my todos list

Example: Todo Fields Values
| title | doneStatus | description |
| test | false | Testdescription |
| test2 | true | hello |
| test3 | false | world |