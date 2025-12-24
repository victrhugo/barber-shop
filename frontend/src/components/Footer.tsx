import { Scissors } from 'lucide-react'

export default function Footer() {
  return (
    <footer className="bg-gray-900 text-white py-8">
      <div className="container mx-auto px-4">
        <div className="flex flex-col md:flex-row justify-between items-center">
          <div className="flex items-center space-x-2 mb-4 md:mb-0">
            <Scissors className="w-6 h-6 text-primary-400" />
            <span className="text-xl font-bold">BarberShop</span>
          </div>
          
          <div className="text-center md:text-left">
            <p className="text-gray-400">
              © 2024 BarberShop. Todos os direitos reservados.
            </p>
          </div>
          
          <div className="flex space-x-4 mt-4 md:mt-0">
            <a href="#" className="text-gray-400 hover:text-white transition">
              Sobre
            </a>
            <a href="#" className="text-gray-400 hover:text-white transition">
              Contato
            </a>
            <a href="#" className="text-gray-400 hover:text-white transition">
              Privacidade
            </a>
          </div>
        </div>
      </div>
    </footer>
  )
}



