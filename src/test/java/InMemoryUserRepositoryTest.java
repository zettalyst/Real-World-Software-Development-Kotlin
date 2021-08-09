import com.iteratrlearning.shu_book.chapter_06.in_memory.InMemoryUserRepository;

public class InMemoryUserRepositoryTest extends com.iteratrlearning.shu_book.chapter_06.AbstractUserRepositoryTest
{
    private InMemoryUserRepository inMemoryUserRepository = new InMemoryUserRepository();

    @Override
    protected UserRepository newRepository()
    {
        return inMemoryUserRepository;
    }
}
