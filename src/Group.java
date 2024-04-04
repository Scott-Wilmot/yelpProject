import java.util.ArrayList;

public class Group {
    Business center;
    ArrayList<Business> group = new ArrayList<>();
    public Group(Business center) {
        this.center = center;
        group.add(center);
    }
    public void addToGroup(Business business) {
        group.add(business);
    }
}
