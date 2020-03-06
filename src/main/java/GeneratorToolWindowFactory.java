import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class GeneratorToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        GeneratorToolWindow generatorToolWindow = new GeneratorToolWindow(toolWindow);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(generatorToolWindow.getContent(), "", false);
        content.setPreferredFocusableComponent(generatorToolWindow.getContent());
        System.err.println("h");
        toolWindow.getContentManager().addContent(content);

    }
}
