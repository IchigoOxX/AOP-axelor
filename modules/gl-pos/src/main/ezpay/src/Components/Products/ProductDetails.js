import React from 'react';
import Popup from '../Common/Popup';
import ProductDetailsContent from "./ProductDetailsContent";

const ProductDetails = props => {
  return (
    <Popup title="Product Details" onClose={props.closeProductDetails}>
      <ProductDetailsContent product={props.product} />
    </Popup>
  );
};

export default ProductDetails;
