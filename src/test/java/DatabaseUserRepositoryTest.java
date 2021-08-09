import com.iteratrlearning.shu_book.chapter_06.database.DatabaseUserRepository;

public class DatabaseUserRepositoryTest extends com.iteratrlearning.shu_book.chapter_06.AbstractUserRepositoryTest
{
    @Override
    protected UserRepository newRepository()
    {
        return new DatabaseUserRepository();
    }
}
