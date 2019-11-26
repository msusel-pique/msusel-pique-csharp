using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Benchmark5Findings
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

        public void Rcs1163(String notUsed)
        {
            Console.WriteLine("In Rcs1163 method");
        }
    }
}
