import com.iteratrlearning.shu_book.chapter_06.database.DatabaseTwootRepository;
import org.junit.Before;

import java.io.IOException;

public class DatabaseTwootRepositoryTest extends com.iteratrlearning.shu_book.chapter_06.AbstractTwootRepositoryTest
{
    @Before
    public void setUp() throws IOException
    {
        repository = new DatabaseTwootRepository();

    }
}
