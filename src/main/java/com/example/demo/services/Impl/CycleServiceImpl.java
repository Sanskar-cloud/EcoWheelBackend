package com.example.demo.services.Impl;

import com.example.demo.entities.Cycle;
import com.example.demo.entities.User;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.payloads.CreateCycleRequest;
import com.example.demo.payloads.CycleDto;
import com.example.demo.payloads.CycleResponse;
import com.example.demo.payloads.UserDto;
import com.example.demo.repository.CycleRepo;
import com.example.demo.repository.UserRepo;
import com.example.demo.services.CycleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CycleServiceImpl implements CycleService {
    @Autowired
    CycleRepo cycleRepo;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    UserRepo userRepo;


    @Override
    public CycleDto addCycle(CreateCycleRequest createCycleRequest) {
        Cycle cycle = this.modelMapper.map(createCycleRequest, Cycle.class);
        cycle.setCycleNumber(createCycleRequest.getCycleNumber());
        cycle.setIsAvailable(createCycleRequest.getAvailable());
        Cycle newCycle = this.cycleRepo.save(cycle);

        return this.modelMapper.map(newCycle, CycleDto.class);
    }

    @Override
    public CycleDto getCycleById(Integer cycleId) {
        Cycle cycle = this.cycleRepo.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Cycle", " Id ", cycleId));

        return this.cycleToDto(cycle);
    }

    @Override
    public CycleResponse getAllCycle(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Sort sort = (sortDir.equalsIgnoreCase("asc")) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable p = PageRequest.of(pageNumber, pageSize, sort);

        Page<Cycle> pagePost = this.cycleRepo.findAll(p);

        List<Cycle> allCycles = pagePost.getContent();

        List<CycleDto> cycleDtos = allCycles.stream().map((cycle) -> this.modelMapper.map(cycle, CycleDto.class))
                .collect(Collectors.toList());

        CycleResponse cycleResponse = new CycleResponse();

        cycleResponse.setContent(cycleDtos);
        cycleResponse.setPageNumber(pagePost.getNumber());
        cycleResponse.setPageSize(pagePost.getSize());
        cycleResponse.setTotalElements(pagePost.getTotalElements());

        cycleResponse.setTotalPages(pagePost.getTotalPages());
        cycleResponse.setLastPage(pagePost.isLast());

        return cycleResponse;


    }

    @Override
    public void deleteCycle(Integer cycleId) {
        Cycle post = this.cycleRepo.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Cycle ", "cycle id", cycleId));

        this.cycleRepo.delete(post);


    }

    @Override
    public List<CycleDto> getCyclesByUser(Integer userId) {
        User user = this.userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User ", "userId ", userId));
        List<Cycle> cycles = this.cycleRepo.findByUser(user);

        List<CycleDto> cycleDtos = cycles.stream().map((cycle) -> this.modelMapper.map(cycle, CycleDto.class))
                .collect(Collectors.toList());

        return cycleDtos;

    }

    @Override
    public List<CycleDto> searchCycle(String keyword) {
        List<Cycle> posts = this.cycleRepo.searchByTitle("%" + keyword + "%");
        List<CycleDto> postDtos = posts.stream().map((post) -> this.modelMapper.map(post, CycleDto.class)).collect(Collectors.toList());
        return postDtos;
    }

    public Cycle dtoToUser(CycleDto cycleDto) {
        Cycle cycle = this.modelMapper.map(cycleDto, Cycle.class);

        // user.setId(userDto.getId());
        // user.setName(userDto.getName());
        // user.setEmail(userDto.getEmail());
        // user.setAbout(userDto.getAbout());
        // user.setPassword(userDto.getPassword());
        return cycle;
    }

    public CycleDto cycleToDto(Cycle cycle) {
        CycleDto cycleDto= this.modelMapper.map(cycle, CycleDto.class);
        return cycleDto;
    }
}
