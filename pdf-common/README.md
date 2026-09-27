# PDF 公共组件（JDK 8）

基于 PDFBox 3.0.8，无 Spring 依赖。独立 POM，可直接复制此模块到公司项目；当前仓库其余模块仍使用 Java 17。

## 构建与接入

```bash
# 使用 Maven 3.6.3+，可在 JDK 8 或更高版本构建，产物目标为 Java 8。
mvn -f pdf-common/pom.xml test
mvn -f pdf-common/pom.xml install
```

业务项目依赖：

```xml
<dependency>
    <groupId>com.kars</groupId>
    <artifactId>pdf-common</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## 字体与 K8s

**模块不捆绑中文字体。** 请将公司允许嵌入、覆盖业务所需字符的静态 TrueType `.ttf` 字体放在业务应用的 `src/main/resources/fonts/chinese-regular.ttf`。不要把任意 OTF、TTC、可变字体直接改扩展名当成 TTF。

支持两种配置：

```java
// 随业务 JAR 打包：直接读取资源流，不依赖宿主机字体。
PdfOptions options = PdfOptions.builder()
        .font("classpath:fonts/chinese-regular.ttf")
        .build();

// 字体复制进镜像或通过卷挂载后，直接使用容器内路径。
PdfOptions mounted = PdfOptions.builder()
        .font("/app/fonts/chinese-regular.ttf")
        .build();
```

支持传入 ClassLoader：`PdfFontSource.classpath("fonts/chinese-regular.ttf", MyApp.class.getClassLoader())`。

容器内无需安装或注册系统字体。字体默认以子集形式嵌入 PDF；缺少资源或所需字形会报错。常规中文之外，生僻字、emoji 等需要字体本身支持；不会自动回退到其他字体。字体资源必须关闭 Maven filtering。

建议业务应用通过环境变量选择字体：

```java
String fontLocation = System.getenv("PDF_FONT_LOCATION");
if (fontLocation == null || fontLocation.trim().isEmpty()) {
    fontLocation = "classpath:fonts/chinese-regular.ttf";
}
PdfOptions options = PdfOptions.builder().font(fontLocation).build();
```

采用镜像文件时，在已有 Dockerfile 中加入：

```dockerfile
COPY fonts/chinese-regular.ttf /app/fonts/chinese-regular.ttf
ENV PDF_FONT_LOCATION=/app/fonts/chinese-regular.ttf
```

如果使用只读根文件系统，优先输出到 HTTP 输出流或字节数组；文件输出的目标目录需要可写。无需为 classpath 字体额外挂载卷。

## 完整使用示例

### 通过测试直接生成 PDF

运行 `PdfSampleTest.generateChineseReport`，配置中文字体即可生成含中文、跨页表格及页码的样例：

```bash
cd pdf-common
mvn -Dtest=PdfSampleTest -Dpdf.font=/path/to/chinese-regular.ttf test
```

默认输出 `pdf-common/target/generated-pdf/sample-report.pdf`。可用 `-Dpdf.output=/path/to/report.pdf` 指定输出文件；字体也支持 `-Dpdf.font=classpath:fonts/chinese-regular.ttf`。在 IDE 中直接运行测试时，在 VM options 中填入同样的 `-Dpdf.font=...`，相对输出路径以 IDE 配置的工作目录为基准。

字体选择顺序：`-Dpdf.font`、`PDF_FONT_LOCATION`、classpath 中的 `fonts/chinese-regular.ttf`，最后尝试本机的 `/Library/Fonts/Arial Unicode.ttf`。因此在安装了该字体的 Mac 上可以直接在 IDEA 点击运行，无需额外参数；控制台会打印使用的字体和 PDF 的绝对路径。

自动寻找本机字体仅用于样例测试，生产组件及 K8s 仍应显式配置字体。没有配置且上述位置均不存在字体时，仅此样例测试跳过；显式提供的字体路径错误或字形缺失会使测试失败。也可以在 IDEA 的 Run/Debug Configurations → VM options 中手动配置 `-Dpdf.font="/Library/Fonts/Arial Unicode.ttf"`。

### 在业务代码中调用

```java
import com.kars.common.pdf.PdfComponents;
import com.kars.common.pdf.PdfGenerator;
import com.kars.common.pdf.PdfOptions;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public class ReportExample {
    public static void main(String[] args) throws IOException {
        PdfOptions options = PdfOptions.builder()
                .font("classpath:fonts/chinese-regular.ttf")
                .title("订单报告")
                .fontSize(12)
                .lineSpacing(1.4f)
                .margins(48, 48, 48, 48) // 左、右、上、下，单位 point
                .build();

        PdfGenerator generator = new PdfGenerator(options, PdfGenerator.pageNumbers(9));
        generator.write(Paths.get("output/orders.pdf"),
                PdfComponents.heading("订单报告", 20),
                PdfComponents.paragraph("客户：小王\n以下是订单明细。"),
                PdfComponents.table(
                        Arrays.asList("商品", "数量", "金额"),
                        Arrays.asList(
                                Arrays.asList("办公用品", "2", "100.00"),
                                Arrays.asList("打印纸", "5", "150.00"))),
                PdfComponents.paragraph("合计：250.00 元"));

        byte[] bytes = generator.generate(PdfComponents.paragraph("内存输出示例"));
        // HTTP 场景：设置 Content-Type: application/pdf 后调用
        // generator.write(response.getOutputStream(), PdfComponents.paragraph("下载内容"));
        // 调用方的输出流不会被关闭。
    }
}
```

其他组件：

```java
PdfComponent report = PdfComponents.heading("附件", 18)
        .andThen(PdfComponents.paragraph("图片说明"));
generator.write(outputStream, report,
        PdfComponents.image(imageBytes, 400, 300),
        PdfComponents.pageBreak(),
        context -> context.text("自定义内容"));
```

显式 `pageBreak()` 始终创建新页，末尾调用会留下空白末页。自动分页不会单独为段后间距创建新页。

## 扩展方式

| 类型 | 用途 |
| --- | --- |
| `PdfGenerator` | 文档生命周期、字节数组/流/文件输出 |
| `PdfOptions` | 不可变页面尺寸、页边距、字号、行距、字体配置 |
| `PdfFontSource` | 字体加载策略，每次创建属于当前文档的字体 |
| `PdfComponent` | 业务模板、段落、图片、表格、图表的组合与扩展 |
| `PdfRenderContext` | 当前页、游标、换行测量、剩余空间、原生绘制 API |
| `PdfPageDecorator` | 正文完成后逐页添加页眉、页脚、水印，能获取总页数 |

自定义组件通过 `ensureSpace(height)` 预留空间、通过 `advance(height)` 移动游标。原生绘图坐标从左下角开始；组件不得关闭上下文的流或文档，改动图形状态后应恢复。

自定义页面装饰器应以 `AppendMode.APPEND` 打开独立内容流并关闭它，不增加或删除页面。页眉页脚应留在预留边距内；内置页码基线为 `字号 × 1.5`，建议下边距至少 `字号 × 3`。

## 能力边界

- 段落保留显式换行，按 Unicode 码点测量、优先在空格处换行；未实现复杂文字塑形、中文避头尾等专业排版规则。
- 基础表格使用等宽列，单元格自动折行，分页时重复表头。暂不支持合并单元格和单行拆页；超高行会报错，避免内容被裁切。
- 标题通过字号区分；需要独立粗体、多字体混排时，可扩展字体策略与内容组件。
- 图片按比例缩放到指定范围及页面可用区域内，整体分页。
- 文件输出先在同目录写临时文件，成功后替换目标；文件系统不支持原子移动时降级为普通替换。
- 字节数组输出将完整 PDF 保存在内存；流输出避免最终字节数组副本，但 PDFBox 文档本身仍占用内存，大报表需结合容器内存限制控制并发。
- 每次生成独立文档；生成器可复用，传入的自定义字体工厂、组件、装饰器也必须无共享可变状态或自行保证线程安全。不要跨文档共享 `PDFont`。

测试覆盖分页、字体嵌入、总页码、表格重复表头、图片、输出流所有权、文件失败保护、字体加载错误、非法布局以及并发生成。测试使用 PDFBox 自带西文字体，不依赖容器系统字体；上线前请用公司的实际中文字体检查包含生僻字的业务样本。

## 本次验证记录

- 主代码和原始测试源码使用 PDFBox 3.0.8 / JUnit 5.12.2 的实际依赖，经 `javac --release 8` 编译。
- 在 JDK 8 上通过 JUnit Platform 运行全部 10 项测试，全部通过，无跳过。样例测试未设置 `pdf.font`，验证了本机字体自动回退。
- 使用本机中文字体生成两页样例，完成文本提取和逐页渲染检查；中文字体未加入源码，生产环境仍应提供业务字体。
- 上述验证直接使用已有本地依赖完成，Maven 插件完整构建流程尚未重新验证。接入公司构建环境后执行上面的 `mvn ... test`，并补充实际部署字体的验收。
