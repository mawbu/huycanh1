package com.hutech.QuanLySP.controller;

import com.hutech.QuanLySP.model.Product;
import com.hutech.QuanLySP.service.CategoryService;
import com.hutech.QuanLySP.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    private final Logger logger = Logger.getLogger(ProductController.class.getName());
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String showProductList(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products/product-list";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam("query") String query, Model model) {
        List<Product> products = productService.searchProducts(query);
        model.addAttribute("products", products);
        return "products/product-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/add-product";
    }

    @PostMapping("/add")
    public String addProduct(@Valid Product newProduct, @RequestParam("imageProduct") MultipartFile imageProduct, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("product", newProduct);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/add-product";
        }

        if (imageProduct != null && !imageProduct.isEmpty()) {
            try {
                Path saveDirectoryPath = Paths.get("src/main/resources/static/images");
                File saveDirectory = new File(saveDirectoryPath.toUri());

                if (!saveDirectory.exists()) {
                    saveDirectory.mkdirs();
                }

                String newImageFile = UUID.randomUUID() + ".png";
                Path path = saveDirectoryPath.resolve(newImageFile);
                Files.copy(imageProduct.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                newProduct.setImage(newImageFile);

                logger.info("Image saved at: " + path.toString());
                logger.info("Product image file name: " + newProduct.getImage());
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("product", newProduct);
                model.addAttribute("categories", categoryService.getAllCategories());
                model.addAttribute("errorMessage", "Error saving image");
                return "products/add-product";
            }
        } else {
            newProduct.setImage(null);
        }

        productService.addProduct(newProduct);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/update-product";
    }


    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id, @Valid Product product,
                                @RequestParam("imageProduct") MultipartFile imageProduct,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            product.setId(id);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/update-product";
        }

        if (imageProduct != null && !imageProduct.isEmpty()) {
            try {
                Path saveDirectoryPath = Paths.get("src/main/resources/static/images");
                File saveDirectory = new File(saveDirectoryPath.toUri());

                if (!saveDirectory.exists()) {
                    saveDirectory.mkdirs();
                }

                String newImageFile = UUID.randomUUID() + ".png";
                Path path = saveDirectoryPath.resolve(newImageFile);
                Files.copy(imageProduct.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                product.setImage(newImageFile);

                logger.info("Image saved at: " + path.toString());
                logger.info("Product image file name: " + product.getImage());
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("product", product);
                model.addAttribute("categories", categoryService.getAllCategories());
                model.addAttribute("errorMessage", "Error saving image");
                return "products/update-product";
            }
        }

        productService.updateProduct(product);
        return "redirect:/products";
    }


    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return "redirect:/products";
    }
}
