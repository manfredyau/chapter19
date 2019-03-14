package redisTests;

import com.ssm.chapter18.config.RootConfig;
import com.ssm.chapter18.pojo.Role;
import com.ssm.chapter18.service.RoleService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RedisAndSpringTest {
    @Test
    public void test1() {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(RootConfig.class);
        RoleService roleService = applicationContext.getBean(RoleService.class);
        Role role = new Role();
        role.setRoleName("role_name_abc");
        role.setNote("note_abc");

        roleService.insertRole(role);

        Role getRole = roleService.getRole(role.getId());
        getRole.setNote("role_note_1_update");
        roleService.updateRole(role);
    }
}
