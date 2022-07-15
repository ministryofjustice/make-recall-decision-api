import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JavaPoiDocumentPlaceholderReplacementServiceTest {

  private JavaPoiDocumentPlaceholderReplacementService documentPlaceholderReplacementService;

  @BeforeEach
  public void setup() {
    documentPlaceholderReplacementService = new JavaPoiDocumentPlaceholderReplacementService();
  }

  @Test
  public void placeholderTest() {
      try {
          documentPlaceholderReplacementService.generateDocFromTemplate();
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
}


