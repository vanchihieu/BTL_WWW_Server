package iuh.fit.se.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import iuh.fit.se.entities.Category;
import iuh.fit.se.repositories.CategoryRepository;

@Component
public class CategoryConverter implements Converter<String, Category>{
	@Autowired
    private CategoryRepository categoryRepository;


    @Override
    public Category convert(String source) {
        Long id = Long.valueOf(source);
        return categoryRepository.findById(id).orElse(null);
    }
}
