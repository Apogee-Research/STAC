package meta.annotations;

import com.tweeter.TwitterApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TwitterApplication.class)
@TestPropertySource(locations="classpath:test.properties")
@WebAppConfiguration
public @interface TwitterTest { }