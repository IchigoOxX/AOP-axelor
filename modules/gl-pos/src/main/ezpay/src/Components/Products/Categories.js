import React, { useEffect, useState } from 'react';
import Category from './Category';
import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown';
import ArrowDropUpIcon from '@mui/icons-material/ArrowDropUp';
import styled from 'styled-components';
import { useDispatch, useSelector } from 'react-redux';
import { fetchCategoriesThunk } from '../Redux/productSlice';

const Container = styled.div`
  display: flex;
  align-items: center;
`;

const Categories = props => {
  const dispatch = useDispatch();
  const categoriesData = useSelector(state => state.products.categories.list);
  const loading = useSelector(state => state.products.categories.loading);
  const error = useSelector(state => state.products.categories.error);

  useEffect(() => {
    dispatch(fetchCategoriesThunk());
  }, [dispatch]);

  const [categories, setCategories] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);

  useEffect(() => {
    const initialCategories = [
      { name: 'ALL', active: true },
      ...categoriesData.map(category => ({
        ...category,
        active: false,
      })),
    ];
    setCategories(initialCategories);
  }, [categoriesData]);
  const handleCategoryClick = index => {
    const newCategories = categories.map((category, i) => ({
      ...category,
      active: i === index,
    }));
    setCategories(newCategories);
    props.setSelectedCategory(newCategories[index].name);
    setShowDropdown(false);
  };

  const toggleDropdown = () => {
    setShowDropdown(!showDropdown);
  };

  const handleMoreCategoryClick = clickedCategoryName => {
    const newCategories = [...categories];
    const clickedCategoryIndex = newCategories.findIndex(category => category.name === clickedCategoryName);
    const lastVisibleCategoryIndex = Math.min(newCategories.length - 2, 4);
    const lastVisibleCategory = newCategories[lastVisibleCategoryIndex];
    const clickedCategory = newCategories[clickedCategoryIndex];

    newCategories.forEach(category => {
      category.active = category.name === clickedCategoryName;
    });

    newCategories[lastVisibleCategoryIndex] = {
      ...clickedCategory,
      active: true,
    };
    newCategories[clickedCategoryIndex] = {
      ...lastVisibleCategory,
      active: false,
    };

    setCategories(newCategories);

    toggleDropdown();

    props.setSelectedCategory(clickedCategoryName);
  };
  const visibleCategories = categories.slice(0, 5);
  const hiddenCategories = categories.slice(5);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <>
      <Container>
        {visibleCategories.map((category, index) => (
          <Category
            key={index}
            name={category.name}
            active={category.active}
            onClick={() => handleCategoryClick(index)}
          />
        ))}
        {hiddenCategories.length > 0 && (
          <>
            {showDropdown ? (
              <ArrowDropUpIcon onClick={toggleDropdown} />
            ) : (
              <ArrowDropDownIcon onClick={toggleDropdown} />
            )}
          </>
        )}
      </Container>
      {showDropdown && (
        <div>
          {hiddenCategories.map((category, index) => (
            <Category
              key={index}
              name={category.name}
              active={category.name === PaymentResponse.selectedCategory}
              onClick={() => handleMoreCategoryClick(category.name)}
            />
          ))}
        </div>
      )}
    </>
  );
};

export default Categories;
