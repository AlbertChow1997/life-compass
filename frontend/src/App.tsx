import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import RequireRole from './components/RequireRole'
import { AuthProvider } from './context/AuthContext'
import ShopListPage from './pages/ShopListPage'
import ShopDetailPage from './pages/ShopDetailPage'
import PostsPage from './pages/PostsPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import MerchantVouchersPage from './pages/MerchantVouchersPage'
import AdminPostsPage from './pages/AdminPostsPage'
import './App.css'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <main className="container">
          <Routes>
            <Route path="/" element={<ShopListPage />} />
            <Route path="/shop/:id" element={<ShopDetailPage />} />
            <Route path="/posts" element={<PostsPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route
              path="/merchant/vouchers"
              element={
                <RequireRole role="MERCHANT">
                  <MerchantVouchersPage />
                </RequireRole>
              }
            />
            <Route
              path="/admin/posts"
              element={
                <RequireRole role="ADMIN">
                  <AdminPostsPage />
                </RequireRole>
              }
            />
          </Routes>
        </main>
      </BrowserRouter>
    </AuthProvider>
  )
}
