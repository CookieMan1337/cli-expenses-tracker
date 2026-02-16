package service;

import domain.Category;
import exception.ValidationException;
import persistence.CategoryRepository;

public class CategoryService {
    private final CategoryRepository catRep = new CategoryRepository();

    //добавление категории
    public void addCategory(String code, String name) {
        try {
            if (checkCategoryExistence(code)) {
                System.out.println("Category already exists");
                throw new ValidationException("Category already exists");
            } else {
                catRep.save(new Category(code, name));
                System.out.println("Category added"); //!to log
                //catRep.findAll().forEach(System.out::println);
            }
        } catch (ValidationException e) {
            //e.printStackTrace();
            System.out.println(e);
        }
    }

    //проверка существования категории
    public boolean checkCategoryExistence(String code) {
        return catRep.findById(code) != null;
    }
}
