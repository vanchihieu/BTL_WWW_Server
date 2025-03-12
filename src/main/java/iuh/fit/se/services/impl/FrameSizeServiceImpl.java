package iuh.fit.se.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iuh.fit.se.entities.FrameSize;
import iuh.fit.se.repositories.FrameSizeRepository;
import iuh.fit.se.services.FrameSizeService;

@Service
public class FrameSizeServiceImpl implements FrameSizeService{
	@Autowired
    FrameSizeRepository frameSizeRepository;
	@Override
	public FrameSize findById(Long id) {
		// TODO Auto-generated method stub
		return frameSizeRepository.findById(id).orElse(null) ;
	}

	@Override
	public List<FrameSize> findAll() {
		// TODO Auto-generated method stub
		return frameSizeRepository.findAll();
	}

	@Override
	public FrameSize save(FrameSize frameSize) {
		return frameSizeRepository.save(frameSize);
	}
}
