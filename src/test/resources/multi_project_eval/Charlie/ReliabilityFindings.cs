using System;
using System.IO;
using System.Reflection;

namespace Charlie
{

    /// <summary>
    /// Expected Finidngs:
    ///     - CA2002: Do not lock on objects with weak identity
    /// </summary>
    class ReliabilityFindings
    {
        void CA2002A()
        {
            lock (typeof(ReliabilityFindings)) { }
        }
        void CA2002B()
        {
            MemoryStream stream = new MemoryStream();
            lock (stream) { }
        }
        void CA2002C()
        {
            lock ("string") { }
        }
        void CA2002D()
        {
            MemberInfo member = this.GetType().GetMember("CA2002A")[0];
            lock (member) { }
        }
        void CA2002E()
        {
            OutOfMemoryException outOfMemory = new OutOfMemoryException();
            lock (outOfMemory) { }
        }
    }
}
