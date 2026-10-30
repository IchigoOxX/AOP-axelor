import React from 'react';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import styled from 'styled-components';
import { decrementQuantity, incrementQuantity, removeFromCart, selectItem } from '../Redux/cartSlice';
import { useDispatch } from 'react-redux';

const Box = styled.div`
  display: grid;
  grid-template-columns: 3fr 1fr 0.5fr;
  align-items: center;
  margin-bottom: 5px;
  border: solid ${props => (props.selected ? '#0C085C' : '#a8d0e7')} 1px;
  border-radius: 15px;
  overflow-wrap: break-word;
`;

const Container = styled.div`
  display: flex;
`;

const Div = styled.div`
  border-radius: 15px 0px 0px 15px;
  background-color: #a8d0e7;
  width: 25px;
`;

const Img = styled.img`
  max-width: 70px;
  padding: 4px;
  object-fit: fill;
`;

const ProductDetails = styled.div`
  padding: 10px;
`;

const ProductName = styled.div`
  color: #0c085c;
  font-size: 15px;
`;

const ProductPrice = styled.div`
  font-size: 12px;
  color: #868686;
`;

const OriginalPrice = styled.div`
  font-size: 12px;
  text-decoration: line-through;
  color: #b0b0b0;
`;

const QuantityContainer = styled.div`
  display: flex;
  align-self: center;
  min-width: 35px;
  border-radius: 5px;
  overflow: hidden;
  & > * {
    padding: 2px;
    width: 18px;
    text-align: center;
  }
`;

const Sign = styled.div`
  background-color: #a8d0e7;
  cursor: pointer;
`;

const CloseIcon = styled.div`
  justify-self: right;
  margin: 5px;
  color: dimgray;
`;

const PurchasedItem = ({ product }) => {
    const dispatch = useDispatch();
    const { product: productDetails, productName, qty, isSelected, discountAmount, discountTypeSelect } = product;

    const getDiscountDisplay = () => {
        if (parseFloat(discountAmount) !== 0 && discountTypeSelect === 1) {
            return discountAmount;
        }
        return productDetails.discountPercentage;
    };

    const calculateDiscountedPrice = () => {
        if (parseFloat(discountAmount) !== 0 && discountTypeSelect === 2) {
            return (productDetails.salePrice - discountAmount).toFixed(3);
        } else if (parseFloat(productDetails.discountAmount) > 0) {
            return (productDetails.salePrice - productDetails.discountAmount).toFixed(3)
        }
        return parseFloat(productDetails.salePrice).toFixed(3);
    };
    console.log('type: ', discountTypeSelect)
    return (
        <Box onClick={() => dispatch(selectItem({ id: productDetails.id }))} selected={isSelected}>
            <Container>
                <Div></Div>
                <Img src={productDetails.imageUrl} alt={productName} />
                <ProductDetails>
                    <ProductName>{productName}</ProductName>
                    {(parseFloat(discountAmount) > 0 && discountTypeSelect === 2) || parseFloat(productDetails.discountAmount) ? (
                        <>
                            <OriginalPrice>{parseFloat(productDetails.salePrice).toFixed(3)} DT</OriginalPrice>
                            <ProductPrice>{calculateDiscountedPrice()} DT</ProductPrice>
                        </>
                    ) : (
                        <ProductPrice>{parseFloat(productDetails.salePrice).toFixed(3)} DT</ProductPrice>
                    )}
                    <ProductPrice>Discount: {getDiscountDisplay()}%</ProductPrice>
                </ProductDetails>
            </Container>
            <QuantityContainer>
                <Sign onClick={() => dispatch(decrementQuantity({ id: productDetails.id }))}>-</Sign>
                <div>{(parseInt(qty))}</div>
                <Sign onClick={() => dispatch(incrementQuantity({ id: productDetails.id }))}>+</Sign>
            </QuantityContainer>
            <CloseIcon onClick={() => dispatch(removeFromCart({ id: productDetails.id }))}>
                <DeleteOutlineOutlinedIcon />
            </CloseIcon>
        </Box>
    );
};

export default PurchasedItem;