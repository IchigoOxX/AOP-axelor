import React, { useEffect, useRef, useState } from 'react';
import styled from 'styled-components';
import BackspaceOutlinedIcon from '@mui/icons-material/BackspaceOutlined';
import { useDispatch, useSelector } from 'react-redux';
import { clearCart, unselectItem, updateCartItems } from '../Redux/cartSlice';

const Container = styled.div`
  display: ${props => (props.extended ? 'grid' : 'block')};
  grid-template-columns: 2.5fr 1fr;
  grid-gap: 5px;
`;

const NumbersContainer = styled.div`
  display: grid;
  grid-template-columns: repeat(3, auto);
  grid-gap: 5px;
`;

const Buttons = styled.div`
  display: grid;
  grid-gap: 3px;
`;

const Button = styled.button`
  height: 32px;
  border-radius: 10px;
  box-shadow: 0px 2px 3px 0px rgba(0, 0, 0, 0.25);
  border: solid #bbbbbb 1px;
  background-color: #fff;
  cursor: pointer;
`;

const Calculator = ({ onButtonClick, additionalButtons }) => {
  const dispatch = useDispatch();
  const cartItems = useSelector(state => state.cart.cartItems);
  const [input, setInput] = useState('');
  const [activeField, setActiveField] = useState('');

  const purchasedItemRef = useRef(null);

  const handleClickOutside = event => {
    event.stopPropagation();
    if (purchasedItemRef.current && !purchasedItemRef.current.contains(event.target)) {
      dispatch(unselectItem());
    }
  };

  useEffect(() => {
    document.addEventListener('mousedown', handleClickOutside);

    const handleKeyDown = event => {
      const { key } = event;
      if (key >= '0' && key <= '9') {
        handleButtonClick(key);
      } else if (key === 'Backspace') {
        handleButtonClick('C'); // Trigger the same logic as the 'C' button
      }
    };

    document.addEventListener('keydown', handleKeyDown);

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [cartItems, activeField]);

  const handleButtonClick = value => {
    setInput(prevInput => {
      let newInput;
      if (value === 'C') {
        newInput = prevInput.slice(0, -1);
        if (newInput === '') newInput = '0'; // Ensure it resets to '0' if empty
      } else {
        newInput = prevInput === '0' ? value : prevInput + value; // Prevents multiple leading zeroes
      }

      const updatedItems = cartItems.map(item => {
        if (item.isSelected) {
          switch (activeField) {
            case 'discount':
              return {
                ...item,
                discountAmount: parseFloat(newInput) || 0, // Sets to 0 if input is NaN
                discountTypeSelect: 1
              };
            case 'quantity':
              return {
                ...item,
                qty: parseInt(newInput) || 0 // Sets to 0 if input is NaN
              };
            case 'price':
              return {
                ...item,
                discountAmount: parseFloat(item.product.salePrice) - parseFloat(newInput) || 0, // Sets to 0 if input is NaN
                discountTypeSelect: 2
              };
            default:
              return item;
          }
        }
        return item;
      });

      dispatch(updateCartItems(updatedItems));
      return newInput;
    });
  };

  const switchField = field => {
    setActiveField(field);
    setInput('');
  };

  return (
      <Container extended={additionalButtons} ref={purchasedItemRef}>
        <NumbersContainer>
          {[1, 2, 3, 4, 5, 6, 7, 8, 9, '.', 0, 'C'].map((key, index) => (
              <Button key={index} onClick={() => (onButtonClick ? onButtonClick(key) : handleButtonClick(key))}>
                {key}
              </Button>
          ))}
        </NumbersContainer>
        {additionalButtons && (
            <Buttons>
              <Button onClick={() => dispatch(clearCart())}>
                <strong>CLEAR CART</strong>
              </Button>
              <Button onClick={() => switchField('discount')}>DISC</Button>
              <Button onClick={() => switchField('quantity')}>QTY</Button>
              <Button onClick={() => switchField('price')}>PRICE</Button>
            </Buttons>
        )}
      </Container>
  );
};

export { Calculator };
