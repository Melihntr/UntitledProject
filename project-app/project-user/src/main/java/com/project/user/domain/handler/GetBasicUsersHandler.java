package com.project.user.domain.handler;

import com.project.common.usecase.UseCaseHandler;
import com.project.user.domain.model.UserModel;
import com.project.user.domain.port.UserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Core business use case handler responsible for retrieving a lightweight list of all users.
 * This handler does not require any input parameters (hence the use of Void) as it 
 * fetches a general, unfiltered dataset.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GetBasicUsersHandler implements UseCaseHandler<List<UserModel>, Void> {

    private final UserPort userPort;

    /**
     * Executes the business logic to retrieve all users.
     *
     * @param input Void (null), as no filtering criteria are currently required.
     * @return A list of internal user domain models.
     */
    @Override
    public List<UserModel> handle(Void input) {
        
        log.info("user.list.request");

        // Delegate the data retrieval request to the infrastructure port
        List<UserModel> users = userPort.getAllUsers();
        
        /*
         * * Enterprise Note (Scalability):
         * Returning an unbounded List<UserModel> is acceptable for administrative dropdowns 
         * in a system with hundreds or thousands of users. However, in a large-scale system 
         * with millions of users, this would cause severe memory issues (OutOfMemoryError). 
         * In such cases, this Use Case should be refactored to accept a Pagination Input 
         * and return a Spring Data Page<UserModel>.
         */
        
        log.info("user.list.success resultCount={}", users.size());
        
        return users;
    }
}
