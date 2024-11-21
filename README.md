# Coupon Management System


## PFA Postman API collection with the source code (.json file)

## What's This? (Intro)
This system is helpful in managing and applying different types of Coupons to the shopping cart in EComm domain where we can get diff amount of discoutn as per applicablity
## What Works
1. **Cart-wise Discount Coupan**:
   - I get a 10% discount if my total cart value is above a certain amount i.e. (100 in this sceneio).

2. **Product-wise Discount COUPAN**:
   - Discounts are applied to specific products in my cart like supose i bought a product from a specified product List I get 20% off on that product.

3. **Buy X Get Y (BxGy) Discount Coupon**:
   - Buy a certain number of items and get some other items for free.
   - The discount is based on the no of items bought from a certain Buy list only, here you get specified amount free (Buy 2 prouct and get 1 product free from Get List).
   - The discount of the eligible products is updated to include the free items.
   - also there is limit to how many times this coupan can be applied at a given time.

## What Doesn't Work
1. **Combining Multiple Coupons**:
   - I can't combine different types of coupons or apply multiple coupons one after the other.

2. **Partial Coupon Application**:
   - If I don't have enough eligible products, the system doesn't handle partial application of coupons.

3. **Coupon Expiry Notifications**:
   - There's no feature to notify me when a coupon is about to expire.
   - But for this we can create a check or make a Scheduler also in Java for which it will check the expiration date for coupon and will move it from Active to Inactive state.

## Limitations
1. **Repetition Limit**:
   - The system respects the repetition limit for BxGy coupons, but it doesn't tell me if the limit is reached.

2. **Error Handling**:
   - Basic error handling is in place, like checking for zero buy quantity, but it could be improved to handle more edge cases and provide better feedback.
  
  Note: Basically Error or warning response to user can be integrated more.

## Assumptions
1. **Coupon Validity**:
   - It assumes that the coupons provided are valid and active. Expired coupons are filtered out before applying. to check this expiration date check is integrated

2. **Product Availability**:
   - It assumes that the products listed in the coupons are available in the cart.

3. **Single Coupon Application**:
   - It assumes that only one coupon is applied to the cart at a time.

## Code Structure
### Main Methods
- `coupanApplicable(Cart cart)`: Returns a list of applicable coupons for the given cart.
- `calcDiscount(Coupan coupan, Cart cart)`: Calculates the discount for a given coupon and cart.
- `calcCartWiseDiscount(Coupan coupan, Cart cart)`: Calculates the cart-wise discount.
- `calcProductWiseDiscount(Coupan coupan, Cart cart)`: Calculates the product-wise discount.
- `calcBxGyDiscount(Coupan coupan, Cart cart)`: Calculates the BxGy discount.
- `applyCoupan(Cart cart, long id)`: Applies the coupon to the cart and updates the cart items with their respective discount.


## Observations detected In Given Requirements

- In given PDF at last payload and resposne for BxGy coupan is given. So in reposne when cart is eligible for getting a free item ,
  it is adding the count of quantity of that free item, rather ot should apply the discount for that item which is same as its MRP or buying price

- I have not considered this requiement and gone with my approach to keep the quantity same and giving discount equals to that item price.

- It's Not mentioned in Requirement that which item / items to give  PRODUCT-WISE coupon discount if we have 2 items from dedicated buying Product List and if 1 product from that list os to be given discount then which item ?

---

