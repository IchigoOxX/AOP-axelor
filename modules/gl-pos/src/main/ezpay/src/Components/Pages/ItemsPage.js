import React, { useState } from 'react';
import styled from 'styled-components';
import ButtonComponent from '../Common/ButtonComponent';
import QrCodeScannerOutlinedIcon from '@mui/icons-material/QrCodeScannerOutlined';
import LocalPrintshopOutlinedIcon from '@mui/icons-material/LocalPrintshopOutlined';
import Scanning from '../Common/Scanning';
import ProductsTable from '../Products/ProductsTable';
import Page from '../Common/Page';

const Container = styled.div`
  margin: 0 30px;
`;

const Header = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

const H2 = styled.h2`
  font-size: 27px;
  color: #0c085c;
`;

const Buttons = styled.div`
  display: flex;
  gap: 7px;
`;

const ItemsPage = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [isScanOpen, setIsScanOpen] = useState(false);

  const openScan = () => {
    setIsScanOpen(true);
  };

  const closeScan = () => {
    setIsScanOpen(false);
  };

  return (
    <Page>
      <Container>
        <Header>
          <H2>Products List</H2>
        </Header>
        <ProductsTable searchQuery={searchQuery} />
        {/* Assuming ProductsTable accepts searchQuery prop for filtering */}
      </Container>
    </Page>
  );
};

export default ItemsPage;
