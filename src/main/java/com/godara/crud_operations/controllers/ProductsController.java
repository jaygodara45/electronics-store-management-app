package com.godara.crud_operations.controllers;


import com.godara.crud_operations.models.Product;
import com.godara.crud_operations.models.ProductDTO;
import com.godara.crud_operations.services.ProductsRepository;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Binding;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductsRepository repo;

    @GetMapping({"", "/"})
    public String showProductsList(Model model){
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "products/index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model){
        ProductDTO productDTO = new ProductDTO();
        model.addAttribute("productDTO", productDTO);
        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute ProductDTO productDTO,
            BindingResult result
    ){
        if(productDTO.getImageFile().isEmpty()){
            result.addError(new FieldError("productDTO", "imageFile", "The image file is empty"));

        }

        if(result.hasErrors()){
            return "products/CreateProduct";
        }

        // save image
        MultipartFile image = productDTO.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + " " + image.getOriginalFilename();


        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if(!Files.exists(uploadPath)){
                Files.createDirectories( uploadPath);
            }

            try(InputStream inputStream = image.getInputStream()){
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception Ex) {
            System.out.println("Exception: " + Ex.getMessage() );
        }
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setBrand(productDTO.getBrand());
        product.setCategory(productDTO.getCategory());
        product.setPrice(productDTO.getPrice());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);

        repo.save(product);

        return "redirect:/products";

    }

    @GetMapping("/edit")
    public String showEditPage(
                Model model,
                @RequestParam int id
        ){

            try {
                Product product = repo.findById(id).get();
                model.addAttribute("product", product);

                ProductDTO productDTO = new ProductDTO();
                productDTO.setName(productDTO.getName());
                productDTO.setBrand(productDTO.getBrand());
                productDTO.setCategory(productDTO.getCategory());
                productDTO.setPrice(productDTO.getPrice());
                productDTO.setDescription(productDTO.getDescription());

                model.addAttribute("productDTO", productDTO);





            }
            catch(Exception Ex) {
                System.out.println("Exception: " + Ex);
                return "redirect:/products";

            }
            return "products/EditProduct";
        }

        @PostMapping("/edit")
        public String updateProduct(
                Model model,
                @RequestParam int id,
                @Valid @ModelAttribute ProductDTO productDTO,
                BindingResult result
        ){

            try {
                Product product = repo.findById(id).get();
                model.addAttribute("product", product);

                if(result.hasErrors()){
                    return "products/EditProduct";
                }

                if(!productDTO.getImageFile().isEmpty()){
                    // deleting old image
                    String uploadDir = "public/images/";
                    Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

                    try{
                        Files.delete(oldImagePath);
                    }
                    catch(Exception Ex){
                        System.out.println("Exception: " + Ex.getMessage());
                    }

                    // save new image file
                    MultipartFile image = productDTO.getImageFile();
                    Date createdAt = new Date();
                    String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                    try(InputStream inputStream = image.getInputStream()){
                        Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
                    }
                    product.setImageFileName(storageFileName);


                }

                product.setName(productDTO.getName());
                product.setBrand(productDTO.getBrand());
                product.setPrice(productDTO.getPrice());
                product.setDescription(productDTO.getDescription());

                repo.save(product);





            }
            catch(Exception Ex) {
                System.out.println("Exception: " + Ex);


            }
            return "redirect:/products";
        }

        @GetMapping("/delete")
        public String deleteProducts(
                @RequestParam int id
               ){

                try{
                    Product product = repo.findById(id).get();
                    //delte product iamge
                    Path imagePath = Paths.get("public/images/" + product.getImageFileName());

                    try{
                        Files.delete(imagePath);
                    }
                    catch(Exception Ex){
                        System.out.println("Exception: " + Ex.getMessage());
                    }

                    // delete product
                    repo.delete(product);

                }
                catch(Exception Ex){
                    System.out.println("Exception: " + Ex.getMessage());
                }
                return "redirect:/products";
        }

}
