package iuh.fit.se.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import iuh.fit.se.entities.Specifications;
import iuh.fit.se.repositories.SpecificationRepository;

public class SpecificationsConverter implements Converter<String, Specifications> {
	@Autowired
    private SpecificationRepository specificationRepository;

    @Override
    public Specifications convert(String source) {
    	Long id = Long.valueOf(source);
        return specificationRepository.findById(id).orElse(null);
    }
}
