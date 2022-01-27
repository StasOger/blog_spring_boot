package com.example.blog.controllers;

import com.example.blog.model.Post;
import com.example.blog.repo.PostRepository;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

@Controller
public class BlogController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/blog")
    public String blogMain(Model model) {
        Iterable<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
        return "blog-main";
    }

    @GetMapping("/blog/add")
    public String blogAdd(Model model) {return "blog-add";}

    @PostMapping("/blog/add")
    public String blogPostAdd(@RequestParam String title, @RequestParam String anons, @RequestParam String full_text, @RequestParam("file") MultipartFile file, @RequestParam("audio") MultipartFile audio, Model model) throws IOException {

        Post post = new Post(title, anons, full_text);
        byte[] bFile = file.getBytes();
        byte[] bAudio = audio.getBytes();

        try (FileOutputStream fos = new FileOutputStream("D:/MUSIC/" + audio.getOriginalFilename())) {
            fos.write(bAudio);
        }
        logger.info("audio saved" + audio.getOriginalFilename());

        post.setAudioPath(audio.getOriginalFilename());
        post.setPhoto(bFile);
        postRepository.save(post);
        return "redirect:/blog";
    }

    private final Logger logger = LoggerFactory.getLogger(BlogController.class);



    @GetMapping("/blog/{id}")
    public String blogDetails(@PathVariable(value = "id") long id, Model model) {
        if(!postRepository.existsById(id)){
            return "redirect:/blog";
        }

        Optional<Post> post = postRepository.findById(id);
        ArrayList<Post> res = new ArrayList<>();
        post.ifPresent(res::add);
        model.addAttribute("post", res);
        return "blog-details";
    }

    @GetMapping("/blog/{id}/edit")
    public String blogEdit(@PathVariable(value = "id") long id, Model model) {
        if(!postRepository.existsById(id)){
            return "redirect:/blog";
        }

        Optional<Post> post = postRepository.findById(id);
        ArrayList<Post> res = new ArrayList<>();
        post.ifPresent(res::add);
        model.addAttribute("post", res);
        return "blog-edit";
    }

    @PostMapping("/blog/{id}/edit")
    public String blogPostUpdate(@PathVariable(value = "id") long id, @RequestParam String title, @RequestParam String anons, @RequestParam String full_text, Model model) {
        Post post = postRepository.findById(id).orElseThrow();
        post.setTitle(title);
        post.setAnons(anons);
        post.setFull_text(full_text);
        postRepository.save(post);

        return "redirect:/blog";
    }

    @PostMapping("/blog/{id}/remove")
    public String blogPostDelete(@PathVariable(value = "id") long id, Model model) {
        Post post = postRepository.findById(id).orElseThrow();
        postRepository.delete(post);

        return "redirect:/blog";
    }

    @GetMapping("/showImage/{id}")
    public void showImage(@PathVariable long id, HttpServletResponse response) throws IOException {
        response.setContentType("image/jpeg");

        Optional<Post> postOptional = postRepository.findById(Long.valueOf(id));
        if (postOptional.isPresent()) {
            InputStream is = new ByteArrayInputStream(postOptional.get().getPhoto());
            IOUtils.copy(is, response.getOutputStream());
        } else {
            logger.error("Post with id:{} was not find", id);
        }
    }

    @GetMapping("/showAudio/{id}")
    public void showAudio(@PathVariable long id, HttpServletResponse response) throws IOException {
        response.setContentType("audio/mp3");

        Optional<Post> postOptional = postRepository.findById(Long.valueOf(id));
        if (postOptional.isPresent()) {

            String audioPath = "D:/MUSIC/" + postOptional.get().getAudioPath();
            InputStream is = new FileInputStream(audioPath);
            IOUtils.copy(is, response.getOutputStream());
        } else {
            logger.error("Post with id:{} was not find", id);
        }
    }




}