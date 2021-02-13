package neu.csye6225.webappone.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import neu.csye6225.webappone.pojo.Book;
import neu.csye6225.webappone.service.BookService;
import neu.csye6225.webappone.utils.auth.UserAuthorization;
import neu.csye6225.webappone.utils.validation.BookRequestBodyValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
@RestController
@RequestMapping("/books")
public class BookController {
    @Autowired
    private BookService bookService;
    @Autowired
    private UserAuthorization userAuthorization;
    @Autowired
    private BookRequestBodyValidator bookRequestBodyValidator;

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'");

    /**
     * This method handles the GET call to /books which gets all books' information.
     */
    @GetMapping(produces = "application/json")
    public @ResponseBody ResponseEntity<?> getAllBooks() {
        List<Book> allBooks = bookService.findAll();
        if (allBooks.isEmpty()) {
            HashMap<String, String> response = new HashMap<>();
            response.put("msg", "There are currently no books.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(allBooks, HttpStatus.OK);
    }

    /**
     * This method handles the GET call to /books/{id} which use book id to get book information.
     */
    @GetMapping(value = "/{id}", produces = "application/json")
    public @ResponseBody ResponseEntity<?> getBookById(HttpServletRequest request, @PathVariable String id) {
        // check for authorization
        String header = request.getHeader("Authorization");
        HashMap<String, String> authResult = userAuthorization.check(header);
        if (!authResult.get("status").equals("200")) { // if auth is invalid
            return noAuthResponse(authResult);
        }

        // check for book id validity
        Book book = bookService.findById(id);
        if (book == null) {
            HashMap<String, String> errMsg = new HashMap<>();
            errMsg.put("error", "There is no book found with id " + id);
            return new ResponseEntity<>(errMsg, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(book.serializeToMap(), HttpStatus.OK);
        }

    }

    /**
     * This method handles the DELETE call to /books which use book id to delete book.
     */
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBookById(HttpServletRequest request, @PathVariable String id) throws Exception{
        // check for authorization
        String header = request.getHeader("Authorization");
        HashMap<String, String> authResult = userAuthorization.check(header);
        if (!authResult.get("status").equals("200")) { // if auth is invalid
            return noAuthResponse(authResult);
        }

        // check for book id validity
        Book book = bookService.findById(id);
        if (book == null) {
            HashMap<String, String> errMsg = new HashMap<>();
            errMsg.put("error", "There is no book found with id " + id);
            return new ResponseEntity<>(errMsg, HttpStatus.NOT_FOUND);
        } else {
            bookService.deleteById(id);
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        }
    }

    /**
     * This method handles the POST call to /books which create a new book.
     * It checks for authorization and user's request body format.
     */
    @PostMapping(produces = "application/json", consumes = "application/json")
    public @ResponseBody ResponseEntity<?> registerUser(HttpServletRequest request,
                                                        @RequestBody String jsonBook) throws JsonProcessingException {
        // check for authorization
        String header = request.getHeader("Authorization");
        HashMap<String, String> authResult = userAuthorization.check(header);
        if (!authResult.get("status").equals("200")) { // if auth is invalid
            return noAuthResponse(authResult);
        }

        // new user information as hashmap
        HashMap<String, String> mapBook = new ObjectMapper().readValue(jsonBook, new TypeReference<>(){});
        // check request body validity
        HashMap<String, String> reqBodyCheckResult = bookRequestBodyValidator.checkForPost(mapBook);
        if (reqBodyCheckResult.containsKey("error")) { // return http 400 if request body is invalid
            return new ResponseEntity<>(reqBodyCheckResult, HttpStatus.BAD_REQUEST);
        }

        // todo: unique id/isbn

        // create new book
        Book tmpBook = new Book(mapBook.get("id"), mapBook.get("title"), mapBook.get("author"),
                mapBook.get("isbn"), mapBook.get("published_date"));
        tmpBook.setUser_id(authResult.get("id"));
        String currTimestamp = formatter.format(new Date());
        tmpBook.setBook_created(currTimestamp);

        // save the book and return response http 201
        bookService.save(tmpBook);
        HashMap<String, String> response = tmpBook.serializeToMap();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * This method process the API response if authorization is invalid.
     */
    private ResponseEntity<?> noAuthResponse(HashMap<String, String> authRes) {
        if (authRes.get("status").equals("400")) { // return http 400 if authentication format is invalid
            authRes.remove("status");
            return new ResponseEntity<>(authRes,HttpStatus.BAD_REQUEST);
        } else { // return http 401 if username or password is invalid
            authRes.remove("status");
            return new ResponseEntity<>(authRes,HttpStatus.UNAUTHORIZED);
        }
    }

}
