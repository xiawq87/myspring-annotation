import com.xwq.ApplicationContext;
import com.xwq.entity.User;
import com.xwq.service.UserService;
import org.junit.Test;

public class MySpringTest {

    @Test
    public void test() {
        UserService userService = ApplicationContext.getBean(UserService.class);
        User user = userService.getUser(1L);
        System.out.println(user);
    }
}
