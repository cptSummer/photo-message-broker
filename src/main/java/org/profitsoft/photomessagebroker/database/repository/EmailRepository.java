package org.profitsoft.photomessagebroker.database.repository;

import org.profitsoft.photomessagebroker.database.data.EmailData;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface EmailRepository extends CrudRepository<EmailData, String> {


  ArrayList <EmailData> findAllByStatus(String status);



}
