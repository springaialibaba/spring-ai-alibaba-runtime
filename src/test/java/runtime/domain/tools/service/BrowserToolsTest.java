package runtime.domain.tools.service;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BrowserToolsTest {

    @Test
    void testNavigateAndSnapshot() {
        Assumptions.assumeTrue(TestUtil.shouldRunIntegration());
        BaseSandboxTools tools = new BaseSandboxTools();

        String nav = tools.browser_navigate("https://cn.bing.com");
        System.out.println("Navigation result: " + nav);
        assertNotNull(nav);

        String snap = tools.browser_snapshot();
        System.out.println("Snapshot result: " + snap);
        assertNotNull(snap);
    }

    @Test
    void testTabAndResize() {
        Assumptions.assumeTrue(TestUtil.shouldRunIntegration());
        BaseSandboxTools tools = new BaseSandboxTools();

        String nav = tools.browser_navigate("https://cn.bing.com");
        System.out.println("Navigation result: " + nav);
        assertNotNull(nav);

        String snap = tools.browser_snapshot();
        System.out.println("Snapshot result: " + snap);
        assertNotNull(snap);

        String newTab = tools.browser_tab_new(null);
        System.out.println("New tab result: " + newTab);
        assertNotNull(newTab);

        String select = tools.browser_tab_select(0);
        System.out.println("Select tab result: " + select);
        assertNotNull(select);

        String resize = tools.browser_resize(1200.0, 800.0);
        System.out.println("Resize result: " + resize);
        assertNotNull(resize);

        String close = tools.browser_close();
        System.out.println("Close result: " + close);
        assertNotNull(close);
    }
}


