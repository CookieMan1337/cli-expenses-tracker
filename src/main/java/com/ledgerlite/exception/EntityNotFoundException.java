package com.ledgerlite.exception;

public class EntityNotFoundException extends LedgerException{
    public EntityNotFoundException(String entityName, Object id){
        super(String.format("%s с ID %s не найден", entityName, id));
    }
}
