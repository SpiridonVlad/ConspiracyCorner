import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ApolloProvider } from '@apollo/client/react';
import { client } from './graphql/client';
import { AuthProvider } from './context/AuthContext';
import Layout from './components/Layout';
import HomePage from './pages/HomePage';
import TheoryDetailPage from './pages/TheoryDetailPage';
import CreateTheoryPage from './pages/CreateTheoryPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import HotTheoriesPage from './pages/HotTheoriesPage';
import MyTheoriesPage from './pages/MyTheoriesPage';

function App() {
  return (
    <ApolloProvider client={client}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/" element={<Layout />}>
              <Route index element={<HomePage />} />
              <Route path="theory/:id" element={<TheoryDetailPage />} />
              <Route path="create" element={<CreateTheoryPage />} />
              <Route path="edit/:id" element={<CreateTheoryPage />} />
              <Route path="login" element={<LoginPage />} />
              <Route path="register" element={<RegisterPage />} />
              <Route path="hot" element={<HotTheoriesPage />} />
              <Route path="my-theories" element={<MyTheoriesPage />} />
            </Route>
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ApolloProvider>
  );
}

export default App;
