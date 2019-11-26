using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml;

namespace TestNetFramework
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("Hello World!");
        }

        public String Scs0005()
        {
            var rnd = new Random();
            byte[] buffer = new byte[16];
            rnd.NextBytes(buffer);
            return BitConverter.ToString(buffer);
        }
    }
}
