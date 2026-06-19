import java.util.Comparator;

public class ToDo implements Comparable<ToDo> {
    String userId;
    String id;
    String title;
    boolean completed;


    public ToDo(String userId, String id, String title, boolean completed) {
        this.completed = completed;
        this.id = id;
        this.title = title;
        this.userId = userId;
    }

    @Override
    public int compareTo(ToDo o) {
        int len1 = this.title.length();
        int len2 = o.title.length();
        if (len1 < len2) return -1;
        else if (len1 > len2) return 1;
        else {
            return this.title.compareTo(o.title);
        }
    }
}