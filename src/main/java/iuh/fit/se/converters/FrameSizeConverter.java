package iuh.fit.se.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import iuh.fit.se.entities.FrameSize;
import iuh.fit.se.repositories.FrameSizeRepository;

public class FrameSizeConverter implements Converter<String, FrameSize>{
	@Autowired
    private FrameSizeRepository frameSizeRepository;

    @Override
    public FrameSize convert(String source) {
    	Long id = Long.valueOf(source);
        return frameSizeRepository.findById(id).orElse(null);
    }
}
