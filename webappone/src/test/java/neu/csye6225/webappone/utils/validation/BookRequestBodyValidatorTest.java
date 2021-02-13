package neu.csye6225.webappone.utils.validation;

import neu.csye6225.webappone.pojo.Book;
import neu.csye6225.webappone.pojo.User;
import neu.csye6225.webappone.service.BookService;
import neu.csye6225.webappone.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BookRequestBodyValidatorTest {
    @Autowired
    private BookRequestBodyValidator bookRequestBodyValidator;

    private Book book = new Book("d6193106-a192-46db-aae9-f151004ee453","Computer Networks",
            "Andrew S. Tanenbaum", "978-0132126953", "May, 2020");
    @Test
    public void checkForPostTest() {
        HashMap<String, String> bookInput = new HashMap<>();
        // invalid id
        bookInput.put("id", "asdf");
        assertEquals(bookRequestBodyValidator.checkForPost(bookInput).get("error"),
                "Please enter a valid 'id' in UUID format!");
        bookInput.put("id", book.getId());
        // invalid title
        assertEquals(bookRequestBodyValidator.checkForPost(bookInput).get("error"),
                "Please do not leave the book 'title' empty!");
        bookInput.put("title", book.getTitle());
        // invalid author
        bookInput.put("author", "");
        assertEquals(bookRequestBodyValidator.checkForPost(bookInput).get("error"),
                "Please do not leave the book 'author' empty!");
        bookInput.put("author", book.getAuthor());
        // invalid isbn
        bookInput.put("isbn", "");
        assertEquals(bookRequestBodyValidator.checkForPost(bookInput).get("error"),
                "Please enter a valid 'isbn' in the format of XXX-XXXXXXXXXX.");
        bookInput.put("isbn", book.getIsbn());
        // invalid password
        bookInput.put("published_date", "may, 202");
        assertEquals(bookRequestBodyValidator.checkForPost(bookInput).get("error"),
                "Please enter 'published_date' in the format of MMM-YYYY, month in characters only.");
        bookInput.put("published_date", book.getPublished_date());
        // successful post
        assertTrue(bookRequestBodyValidator.checkForPost(bookInput).containsKey("ok"));
    }
}