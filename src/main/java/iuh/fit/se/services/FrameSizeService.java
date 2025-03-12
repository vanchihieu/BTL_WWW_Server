package iuh.fit.se.services;

import java.util.List;

import iuh.fit.se.entities.FrameSize;

public interface FrameSizeService {
	public FrameSize findById(Long id);
	public List<FrameSize> findAll();
	public FrameSize save(FrameSize frameSize);
}
