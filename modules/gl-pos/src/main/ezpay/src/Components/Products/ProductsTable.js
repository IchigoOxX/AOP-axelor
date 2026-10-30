import React, { useEffect, useState } from "react";
import styled from "styled-components";
import { useSelector } from "react-redux";
import SwapVertOutlinedIcon from "@mui/icons-material/SwapVertOutlined";
import { useNavigate } from "react-router-dom";
import { Slider } from "@mui/material";

const Container = styled.div`
  margin-bottom: 20px;
`;

const ProductsTableStyled = styled.table`
  margin: 0 20px;
  background-color: #f1f7fb;
  border-radius: 10px;
  width: calc(100% - 40px);
  overflow: auto;
  text-align: left;
  border-collapse: collapse;
  & > th,
  tr {
    padding: 10px;
  }
`;

const TableRow = styled.tr`
  border-top: solid lightgrey 1px;
  &:hover {
    background-color: #e0f3ff;
    cursor: pointer;
  }
`;

const Th = styled.th`
  padding: 7px;
`;

const Td = styled.td`
  padding: 5px;
`;

const Input = styled.input`
  border: none;
  outline-style: none;
  background-color: transparent;
  width: 100%;
  max-width: 120px;
`;

const SortIcon = styled(SwapVertOutlinedIcon)`
  vertical-align: middle;
  margin: 5px;
  cursor: pointer;
  opacity: ${props => props.active ? 1 : 0.5};
`;

const ProductsTable = () => {
  const navigate = useNavigate();
  const products = useSelector((state) => state.products.products.list);

  const [searchFields, setSearchFields] = useState({
    name: "",
    brand: "",
    category: "",
    newArrival: "",
    onPromotion: "",
    bestSeller: "",
    inStock: "",
  });

  const [sortBy, setSortBy] = useState(null); // null, 'name', 'price'
  const [isAscending, setIsAscending] = useState(true);
  const [priceRange, setPriceRange] = useState({
    minPrice: 0,
    maxPrice: 0,
    filteredMinPrice: 0,
    filteredMaxPrice: 0,
  });

  useEffect(() => {
    // Calculate min and max price from products list
    if (products.length > 0) {
      const prices = products.map((product) => parseFloat(product.salePrice)).filter(price => !isNaN(price));
      const minPrice = Math.min(...prices);
      const maxPrice = Math.max(...prices);
      setPriceRange({
        ...priceRange,
        minPrice,
        maxPrice,
        filteredMinPrice: minPrice,
        filteredMaxPrice: maxPrice,
      });
    }
  }, [products]);

  const handleSearchChange = (field) => (event) => {
    setSearchFields({ ...searchFields, [field]: event.target.value });
  };

  const handleCheckboxChange = (field, value) => {
    setSearchFields(prevSearchFields => ({
      ...prevSearchFields,
      [field]: prevSearchFields[field] === value ? "" : value
    }));
  };

  const handlePriceRangeChange = (event, newValue) => {
    setPriceRange({
      ...priceRange,
      filteredMinPrice: newValue[0],
      filteredMaxPrice: newValue[1],
    });
  };

  const handleSortByName = () => {
    if (sortBy !== 'name') {
      setSortBy('name');
      setIsAscending(true);
    } else {
      setIsAscending(!isAscending);
    }
  };

  const handleSortByPrice = () => {
    if (sortBy !== 'price') {
      setSortBy('price');
      setIsAscending(true);
    } else {
      setIsAscending(!isAscending);
    }
  };

  const sortedProducts = [...products].sort((a, b) => {
    if (sortBy === 'name') {
      const nameA = a.name || '';
      const nameB = b.name || '';
      return isAscending
          ? nameA.localeCompare(nameB)
          : nameB.localeCompare(nameA);
    } else if (sortBy === 'price') {
      const priceA = parseFloat(a.salePrice);
      const priceB = parseFloat(b.salePrice);
      return isAscending ? priceA - priceB : priceB - priceA;
    }
    return 0;
  });

  const filteredProducts = sortedProducts.filter((product) => {
    const {
      name,
      brand,
      category,
      newArrival,
      onPromotion,
      bestSeller,
      inStock,
    } = searchFields;
    const { filteredMinPrice, filteredMaxPrice } = priceRange;
    const productName = (product.name || "").toLowerCase();
    const productBrand = (product.brand || "").toLowerCase();
    const productCategory = (product.category?.name || "").toLowerCase();

    return (
        productName.includes(name.toLowerCase()) &&
        productBrand.includes(brand.toLowerCase()) &&
        productCategory.includes(category.toLowerCase()) &&
        (newArrival === "" || !!product.newArrival === (newArrival === "yes")) &&
        (onPromotion === "" || !!product.onPromotion === (onPromotion === "yes")) &&
        (bestSeller === "" || !!product.bestSeller === (bestSeller === "yes")) &&
        (inStock === "" || !!product.inStock === (inStock === "yes")) &&
        parseFloat(product.salePrice) >= filteredMinPrice &&
        parseFloat(product.salePrice) <= filteredMaxPrice
    );
  });

  const handleProductClick = (product) => {
    navigate(`/items/item-details`, { state: { product } });
  };

  return (
      <Container>
        <ProductsTableStyled>
          <thead>
          <tr>
            <Th>
              Name
              <SortIcon
                  onClick={handleSortByName}
                  active={sortBy === 'name'}
              />
            </Th>
            <Th>Brand</Th>
            <Th>
              Price
              <SortIcon
                  onClick={handleSortByPrice}
                  active={sortBy === 'price'}
              />
            </Th>
            <Th>Category</Th>
            <Th>New Arrival</Th>
            <Th>On Promotion</Th>
            <Th>Best Seller</Th>
            <Th>In Stock</Th>
          </tr>
          <tr>
            <Td>
              <Input
                  type="text"
                  value={searchFields.name}
                  onChange={handleSearchChange("name")}
                  placeholder="Search Name"
              />
            </Td>
            <Td>
              <Input
                  type="text"
                  value={searchFields.brand}
                  onChange={handleSearchChange("brand")}
                  placeholder="Search Brand"
              />
            </Td>
            <Td>
              <Slider
                  getAriaLabel={() => "Price range"}
                  value={[
                    priceRange.filteredMinPrice,
                    priceRange.filteredMaxPrice,
                  ]}
                  min={priceRange.minPrice}
                  max={priceRange.maxPrice}
                  onChange={handlePriceRangeChange}
                  valueLabelDisplay="auto"
                  size="small"
                  color="grey[300]"
                  sx={{ color: "dimgrey" }}
              />
            </Td>
            <Td>
              <Input
                  type="text"
                  value={searchFields.category}
                  onChange={handleSearchChange("category")}
                  placeholder="Search Category"
              />
            </Td>
            <Td>
              <input
                  type="checkbox"
                  checked={searchFields.newArrival === "yes"}
                  onChange={() => handleCheckboxChange("newArrival", "yes")}
              />
              Yes
              <input
                  type="checkbox"
                  checked={searchFields.newArrival === "no"}
                  onChange={() => handleCheckboxChange("newArrival", "no")}
              />
              No
            </Td>
            <Td>
              <input
                  type="checkbox"
                  checked={searchFields.onPromotion === "yes"}
                  onChange={() => handleCheckboxChange("onPromotion", "yes")}
              />
              Yes
              <input
                  type="checkbox"
                  checked={searchFields.onPromotion === "no"}
                  onChange={() => handleCheckboxChange("onPromotion", "no")}
              />
              No
            </Td>
            <Td>
              <input
                  type="checkbox"
                  checked={searchFields.bestSeller === "yes"}
                  onChange={() => handleCheckboxChange("bestSeller", "yes")}
              />
              Yes
              <input
                  type="checkbox"
                  checked={searchFields.bestSeller === "no"}
                  onChange={() => handleCheckboxChange("bestSeller", "no")}
              />
              No
            </Td>
            <Td>
              <input
                  type="checkbox"
                  checked={searchFields.inStock === "yes"}
                  onChange={() => handleCheckboxChange("inStock", "yes")}
              />
              Yes
              <input
                  type="checkbox"
                  checked={searchFields.inStock === "no"}
                  onChange={() => handleCheckboxChange("inStock", "no")}
              />
              No
            </Td>
          </tr>
          </thead>
          <tbody>
          {filteredProducts.map((product) => (
              <TableRow key={product.id} onDoubleClick={() => handleProductClick(product)}>
                <Td>{product.name}</Td>
                <Td>{product.brand}</Td>
                <Td>{(parseFloat(product.salePrice)).toFixed(3)}</Td>
                <Td>{product.category?.name}</Td>
                <Td>{product.newArrival ? "Yes" : "No"}</Td>
                <Td>{product.onPromotion ? "Yes" : "No"}</Td>
                <Td>{product.bestSeller ? "Yes" : "No"}</Td>
                <Td>{product.inStock ? "Yes" : "No"}</Td>
              </TableRow>
          ))}
          </tbody>
        </ProductsTableStyled>
      </Container>
  );
};

export default ProductsTable;
