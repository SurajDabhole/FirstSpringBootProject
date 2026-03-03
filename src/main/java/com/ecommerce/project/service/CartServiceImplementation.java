package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.DuplicateItemException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.utils.AuthUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@NoArgsConstructor
@AllArgsConstructor
public class CartServiceImplementation implements CartService {
    private CartRepository cartRepository;
    private AuthUtil authUtil;
    private ProductRepository productRepository;
    private CartItemRepository cartItemRepository;
    private ModelMapper modelMapper;

    @Autowired
    public CartServiceImplementation(CartItemRepository cartItemRepository, CartRepository cartRepository, AuthUtil authUtil, ProductRepository productRepository, ModelMapper modelMapper) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.authUtil = authUtil;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // Find existing cart or create a new cart for the user
        Cart cart = createCart();

        // Retrieve the product details using the productId
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // Perform necessary checks (e.g., product availability, stock quantity)
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(
                cart.getCartId(),
                productId
        );

        if(cartItem != null){
            throw new DuplicateItemException("Product" , "productId", productId);
        }

        if(product.getQuantity() == 0){
            throw new APIException("Product is out of stock");
        }

        if(product.getQuantity() < quantity){
            throw new APIException("Requested quantity exceeds available stock! Please enter a quantity less than or equal to " + product.getQuantity() + ".");
        }

        // Create cart item with the product and quantity, Add the cart item to the cart
        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        // Save the cart to the database
        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepository.save(cart);

        // Return the updated cart details as a CartDTO
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();
        Stream<ProductDTO> productDTOStream =  cartItems.stream().map(item -> {
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDTO.setProducts(productDTOStream.toList());

        return cartDTO;
    }

    public Cart createCart(){
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }

        Cart cart = new Cart();
        cart.setUser(authUtil.loggedInUser());
        cart.setTotalPrice(0.0);

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if(carts.isEmpty()){
            throw new ResourceNotFoundException("Carts not found");
        }

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
            cart.getCartItems().forEach(item -> item.getProduct().setQuantity(item.getQuantity()));
            List<CartItem> cartItems = cart.getCartItems();
            Stream<ProductDTO> productDTOStream =  cartItems.stream().map(item -> {
                ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
                map.setQuantity(item.getQuantity());
                return map;
            });

            cartDTO.setProducts(productDTOStream.toList());

            return cartDTO;
        }).toList();

        return cartDTOs;
    }

    @Override
    @Transactional
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);

        if(cart == null){
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();
        Stream<ProductDTO> productDTOStream =  cartItems.stream().map(item -> {
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });
        cartDTO.setProducts(productDTOStream.toList());

        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        String emailId = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(emailId);
        if(userCart == null){
            throw new ResourceNotFoundException("Cart not found for the current user");
        }
        Long cartId = userCart.getCartId();
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if(product.getQuantity() == 0){
            throw new APIException("Product is out of stock");
        }

        if(product.getQuantity() < quantity){
            throw new APIException("Requested quantity exceeds available stock! Please enter a quantity less than or equal to " + product.getQuantity() + ".");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if(cartItem == null){
            throw  new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        int newQuantity = cartItem.getQuantity() + quantity;
        if(newQuantity < 0){
            throw new APIException("Quantity cannot be negative! Please enter a quantity greater than or equal to " + (-cartItem.getQuantity()) + ".");
        }

        if(newQuantity == 0){
            deleteProductFromCart(cartId, productId);
        }
        else{
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (product.getPrice() * quantity));
            cartRepository.save(cart);
        }

        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        if(updatedCartItem == null){
            throw new APIException("Failed to update cart item");
        }

        if(updatedCartItem.getQuantity() == 0){
            cartItemRepository.deleteById(updatedCartItem.getCartItemId());
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productDTOStream =  cartItems.stream().map(item -> {
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });
        cartDTO.setProducts(productDTOStream.toList());

        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if(cartItem == null){
            throw new ResourceNotFoundException("Cart item not found for the given cartId and productId");
        }
        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartRepository.save(cart);
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);
        return "Product with id " + productId + " has been removed from cart with id " + cartId;
    }
}
