//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
void main() {


    List<ToDo> input = new ArrayList<>();
    ToDo todo1 = new ToDo("1", "1", "b task", false);
    ToDo todo2 = new ToDo("1", "1", "alpha", false);
    ToDo todo3 = new ToDo("1", "1", "a", true);
    ToDo todo4 = new ToDo("1", "1", "go", false);
    ToDo todo5 = new ToDo("1", "1", "zebra", false);

    input.add(todo1);
    input.add(todo2);
    input.add(todo3);
    input.add(todo4);
    input.add(todo5);

    System.out.println(topKIncompleteTitles(input, 3));
}

/*
You are given a user ID and an integer `k`.
Fetch that user’s todos from the following API:
https://jsonplaceholder.typicode.com/todos?userId=
Each todo item has the structure:

```json
{
  "userId": 1,
  "id": 1,
  "title": "delectus aut autem",
  "completed": false
}
Return the titles of the top k incomplete todos (completed == false), sorted by:
	0.	Shortest title length
	0.	Lexicographically ascending order if lengths are equal
If there are fewer than k incomplete todos, return all of them.

Example
Input
userId = 1
k = 3
Suppose incomplete todos are:
["b task", "alpha", "go", "zebra"]
Output
["go", "alpha", "b task"]
["go", "alpha", "zebra"]

 */
public static List<String> topKIncompleteTitles(List<ToDo> todos, int k) {
    List<ToDo> incompleteTodos = todos.stream().filter(x -> !x.completed).toList();

    PriorityQueue<ToDo> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

    for (int i = 0; i < incompleteTodos.size(); i++) {
        if (maxHeap.size() < k) {
            maxHeap.offer(incompleteTodos.get(i));
        } else {
            if (maxHeap.peek().compareTo(incompleteTodos.get(i)) > 0) {
                maxHeap.poll();
                maxHeap.offer(incompleteTodos.get(i));
            }
        }
    }

    List<String> output = new ArrayList<>();
    int outputSize = maxHeap.size();
    for (int i = 0; i < outputSize; i++) {
        output.add(maxHeap.poll().title);
    }

    return output;
}























