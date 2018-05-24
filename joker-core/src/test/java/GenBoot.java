
import com.jsen.joker.boot.RootVerticle;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/10
 */
public class GenBoot {
    @Test
    public void genBootSH() {
        File file = new File("/Users/jsen/Documents/GitProjects/Test/hockvertx/lib");
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (File f: files) {
            if (f.isFile() && f.getName().endsWith(".jar") && !f.getName().endsWith("-fat.jar")) {
                builder.append("./lib/").append(f.getName()).append(":");
            }
        }
        String p = builder.toString();
        if(p.length() > 0) {
            p = p.substring(0, p.length() - 1);
        }
        p = "vertx start " + RootVerticle.class.getName() + " -cp " + p;
        try {
            FileWriter fileWriter = new FileWriter(new File("/Users/jsen/Documents/GitProjects/Test/hockvertx/boot.sh"));
            fileWriter.write(p);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFatRead() {
        File file = new File("/Users/jsen/Documents/GitProjects/Test/hockvertx/boot/target/boot-fat.jar!/verticles.json");
        System.out.println(file.exists());
    }
}
