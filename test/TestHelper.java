package test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public abstract class TestHelper {
    protected String path;

    public TestHelper(String path) {
        this.path = path;
    }
    
    protected abstract boolean testData(int timeOut);

	@Test
	public void testInstance() throws Exception {
		System.out.println(path);
		try {
			assertTrue(testData(90));
		} catch(NullPointerException e) {
			fail("Timed out");
		} catch(OutOfMemoryError e) {
			fail("Not enough memory");
		}
	}

    public static Object[] dataFromFolder(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        List<Object> out = new LinkedList<>();
        assert listOfFiles != null;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                String name = listOfFile.getAbsolutePath();
                if(name.endsWith(".clq") || name.endsWith(".wcnf")) {
                	out.add(name);
                }
            }
        }
        return out.toArray();
    }
    
}
