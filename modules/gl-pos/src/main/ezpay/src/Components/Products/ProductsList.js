import React, { useEffect } from 'react';
import ProductCard from './ProductCard';
import styled from 'styled-components';
import { useDispatch, useSelector } from 'react-redux';
import { fetchProductsThunk } from '../Redux/productSlice';

const Container = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
  padding: 10px;
  max-height: calc(100vh - 155px);
  overflow-y: auto;
`;

const ProductsList = ({ products }) => {
  const dispatch = useDispatch();
  const loading = useSelector(state => state.products.products.loading);
  const error = useSelector(state => state.products.products.error);

  useEffect(() => {
    dispatch(fetchProductsThunk());
  }, [dispatch]);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
      <Container>
        {products.map((product, index) => (
            <ProductCard key={index} product={product} />
        ))}
      </Container>
  );
};

export default ProductsList;
