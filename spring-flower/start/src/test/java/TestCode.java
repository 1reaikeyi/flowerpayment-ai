import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import org.junit.jupiter.api.Test;

import java.io.File;

public class TestCode {
    @Test
    public void testCircleCaptcha() {
        // 圆圈干扰验证码（宽200，高100，验证码长度4，干扰圆圈数量10）
        CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(200, 100, 4, 10);
        String code = circleCaptcha.getCode();
        System.out.println("圆圈验证码: " + code);
        circleCaptcha.write(new File("src/test/java/captcha_circle.png"));
        System.out.println("已生成 src/test/java/captcha_circle.png");
    }
    @Test
    public void testLineCaptcha() {
        // 线段干扰验证码（通过 CodeGenerator 自定义文字，宽200，高100，干扰线10）
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100,
                new CodeGenerator() {
                    @Override
                    public String generate() { return "hjkl"; }
                    @Override
                    public boolean verify(String code, String userInputCode) {
                        return code.equalsIgnoreCase(userInputCode);
                    }
                }, 10);
        System.out.println("自定义验证码: hjkl");
        lineCaptcha.write(new File("src/test/java/captcha_line.png"));
        System.out.println("已生成 src/test/java/captcha_line.png");
    }



    @Test
    public void testShearCaptcha() {
        // 扭曲干扰验证码（构造方法直接传自定义文字：宽200，高100，自定义码，干扰线数量1）
        ShearCaptcha shearCaptcha = new ShearCaptcha(200, 100, new CodeGenerator() {
            @Override
            public String generate() {
                return "hjkl";
            }

            @Override
            public boolean verify(String code, String userInputCode) {
                return code.equalsIgnoreCase(userInputCode);
            }
        }, 10);
        System.out.println("扭曲验证码: hjkl");
        shearCaptcha.write(new File("src/test/java/captcha_shear.png"));
        System.out.println("已生成 src/test/java/captcha_shear.png");
    }

}
