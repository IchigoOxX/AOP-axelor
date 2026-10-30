import React, { useState } from 'react';
import styled from 'styled-components';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import ProductDetails from './ProductDetails';
import { useDispatch } from 'react-redux';
import { addToCart } from '../Redux/cartSlice';

const Container = styled.div`
  position: relative;
  display: flex;
  flex-direction: column;
  height: 220px;
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 8px;
  box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.1);
  font-size: 14px;
  word-wrap: break-word;
  cursor: pointer;
  background-color: white;
`;

const DetailsIcon = styled(InfoOutlinedIcon)`
  position: absolute;
  right: 4px;
  top: 4px;
  color: #868686;
  background-color: white;
  border-radius: 50%;
`;

const Image = styled.img`
  width: 100%;
  height: 100px;
  margin: auto;
  object-fit: contain;
  border-radius: 8px;
`;

const InfoContainer = styled.div`
  display: flex;
  flex-direction: column;
  flex: 1;
  margin-top: 10px;
`;

const Title = styled.div`
  margin-top: 10px;
  font-weight: bold;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
`;

const Price = styled.div`
  font-weight: bold;
  margin-top: 5px;
`;

const Discount = styled.div`
  font-size: 12px;
  color: green;
  margin-top: 5px;
`;

const OriginalPrice = styled.div`
  font-weight: bold;
  margin-top: 5px;
  text-decoration: line-through;
  color: #b0b0b0;
`;

const Stock = styled.div`
  margin-top: auto;
  font-weight: bold;
  color: ${({ qty }) => (qty === 0 ? 'red' : 'inherit')};
`;

const ProductCard = ({ product }) => {
    const dispatch = useDispatch();
    const [showProductDetails, setShowProductDetails] = useState(false);

    const openProductDetails = event => {
        event.stopPropagation();
        setShowProductDetails(true);
    };

    const closeProductDetails = event => {
        event.stopPropagation();
        setShowProductDetails(false);
    };

    const calculateDiscountedPrice = (originalPrice, discountAmount) => {
        return parseFloat(originalPrice - discountAmount).toFixed(3);
    };

    return (
        <>
            <Container onClick={() => dispatch(addToCart({ product }))}>
                <DetailsIcon fontSize="smaller" onClick={openProductDetails} />
                <Image src={product.imageUrl} alt={product.name} />
                <InfoContainer>
                    <Title>{product.name}</Title>
                    {parseFloat(product.discountPercentage) > 0 ? (
                        <>
                            <Price>
                                {parseFloat(product.salePrice).toFixed(3)} DT
                            </Price>
                            <Discount>{parseFloat(product.discountPercentage)}% OFF</Discount>
                        </>
                    ) : parseFloat(product.discountAmount) > 0 ? (
                        <>
                            <OriginalPrice>{parseFloat(product.salePrice).toFixed(3)} DT</OriginalPrice>
                            <Price>
                                {calculateDiscountedPrice(parseFloat(product.salePrice), parseFloat(product.discountAmount))} DT
                            </Price>
                        </>
                    ) : (
                        <Price>{parseFloat(product.salePrice).toFixed(3)} DT</Price>
                    )}
                </InfoContainer>
                <Stock qty={product.stock}>Qty: {parseInt(product.stock)}</Stock>
            </Container>
            {showProductDetails && <ProductDetails product={product} closeProductDetails={closeProductDetails} />}
        </>
    );
};

export default ProductCard;
